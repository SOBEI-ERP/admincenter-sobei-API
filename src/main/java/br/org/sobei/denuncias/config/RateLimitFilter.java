package br.org.sobei.denuncias.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Filtro de Rate Limiting por IP.
 * Limita requisições para prevenir abuso e ataques de força bruta.
 * <p>
 * Limites:
 * - Rotas admin (/api/admin/**): 5 requisições por janela de 10 segundos por IP
 * - Rotas públicas (/api/public/**): 5 requisições por janela de 10 segundos por IP
 * - Swagger/docs: sem limite
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int REQUEST_LIMIT = 5;
    private static final long WINDOW_MS = 10_000L; // 10 segundos
    private static final int RETRY_AFTER_SECONDS = 10;

    private final Map<String, ConcurrentLinkedDeque<Long>> requestCounts = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        // Limpeza periódica de IPs inativos para evitar memory leak
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(this::cleanupStaleEntries, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Sem limite para HTTP OPTIONS (preflight CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // Sem limite para Swagger e docs
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!path.startsWith("/api/admin/") && !path.startsWith("/api/public/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String key = clientIp + ":" + (path.startsWith("/api/admin/") ? "admin" : "public");

        if (isRateLimited(key, REQUEST_LIMIT)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(RETRY_AFTER_SECONDS));
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Muitas requisições. Tente novamente em " + RETRY_AFTER_SECONDS + " segundos.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String key, int limit) {
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_MS;

        ConcurrentLinkedDeque<Long> timestamps = requestCounts.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // Remove timestamps antigos da ponta esquerda de forma muito eficiente O(1)
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= limit) {
            return true;
        }

        timestamps.addLast(now);
        return false;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Pega o primeiro IP (IP real do cliente)
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void cleanupStaleEntries() {
        long windowStart = System.currentTimeMillis() - WINDOW_MS;
        requestCounts.entrySet().removeIf(entry -> {
            ConcurrentLinkedDeque<Long> timestamps = entry.getValue();
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.pollFirst();
            }
            return timestamps.isEmpty();
        });
    }
}
