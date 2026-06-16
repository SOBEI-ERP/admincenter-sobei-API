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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Filtro de Rate Limiting por IP.
 * Limita requisições para prevenir abuso e ataques de força bruta.
 * <p>
 * Limites:
 * - Rotas admin (/api/admin/**): 50 requisições por minuto por IP
 * - Rotas públicas (/api/public/**): 20 requisições por minuto por IP
 * - Swagger/docs: sem limite
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int ADMIN_LIMIT = 50;
    private static final int PUBLIC_LIMIT = 20;
    private static final long WINDOW_MS = 60_000L; // 1 minuto

    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();

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
        String path = request.getRequestURI();

        // Sem limite para Swagger e docs
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        int limit;
        if (path.startsWith("/api/admin/")) {
            limit = ADMIN_LIMIT;
        } else if (path.startsWith("/api/public/")) {
            limit = PUBLIC_LIMIT;
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String key = clientIp + ":" + (path.startsWith("/api/admin/") ? "admin" : "public");

        if (isRateLimited(key, limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Muitas requisições. Tente novamente em 1 minuto.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String key, int limit) {
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_MS;

        List<Long> timestamps = requestCounts.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());

        // Remove timestamps fora da janela
        timestamps.removeIf(ts -> ts < windowStart);

        if (timestamps.size() >= limit) {
            return true;
        }

        timestamps.add(now);
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
            entry.getValue().removeIf(ts -> ts < windowStart);
            return entry.getValue().isEmpty();
        });
    }
}
