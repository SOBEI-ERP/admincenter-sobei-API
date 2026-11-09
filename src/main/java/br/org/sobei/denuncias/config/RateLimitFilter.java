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
import java.util.regex.Pattern;

/**
 * Filtro de Rate Limiting por IP com proteção contra bypass de headers e exaustão de memória.
 * Limita requisições para prevenir abuso e ataques de força bruta.
 *
 * Limites:
 * - Rotas admin (/api/admin/**): 30 requisições por janela de 10 segundos por IP
 * - Rotas públicas (/api/public/**): 10 requisições por janela de 10 segundos por IP
 * - Swagger/docs: sem limite
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int PUBLIC_REQUEST_LIMIT = 10;
    private static final int ADMIN_REQUEST_LIMIT = 30;
    private static final long WINDOW_MS = 10_000L; // 10 segundos
    private static final int RETRY_AFTER_SECONDS = 10;
    private static final int MAX_TRACKED_IPS = 10_000;

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^[0-9a-fA-F:]{2,39}$"
    );

    private final Map<String, ConcurrentLinkedDeque<Long>> requestCounts = new ConcurrentHashMap<>();

    public RateLimitFilter() {
        // Limpeza periódica de IPs inativos para evitar memory leak
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(this::cleanupStaleEntries, 3, 3, TimeUnit.MINUTES);
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

        boolean isAdminRoute = path.startsWith("/api/admin/");
        boolean isPublicRoute = path.startsWith("/api/public/");

        if (!isAdminRoute && !isPublicRoute) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String key = clientIp + ":" + (isAdminRoute ? "admin" : "public");
        int limit = isAdminRoute ? ADMIN_REQUEST_LIMIT : PUBLIC_REQUEST_LIMIT;

        if (isRateLimited(key, limit)) {
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

        // Se atingir o teto de entradas, força a limpeza imediata para proteger a memória heap
        if (requestCounts.size() >= MAX_TRACKED_IPS) {
            cleanupStaleEntries();
            if (requestCounts.size() >= MAX_TRACKED_IPS) {
                requestCounts.clear(); // Prevenção absoluta contra DoS de memória
            }
        }

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
        // 1. Cloudflare connecting IP (prioridade máxima se presente)
        String cfIp = request.getHeader("CF-Connecting-IP");
        if (isValidIp(cfIp)) {
            return cfIp.trim();
        }

        // 2. X-Real-IP
        String xRealIp = request.getHeader("X-Real-IP");
        if (isValidIp(xRealIp)) {
            return xRealIp.trim();
        }

        // 3. X-Forwarded-For (pega o primeiro IP e valida formato)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String firstIp = xForwardedFor.split(",")[0].trim();
            if (isValidIp(firstIp)) {
                return firstIp;
            }
        }

        // 4. Fallback confiável para o socket remoto direto
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr : "unknown";
    }

    private boolean isValidIp(String ip) {
        if (ip == null || ip.isBlank() || ip.length() > 45) {
            return false;
        }
        String trimmed = ip.trim();
        return IPV4_PATTERN.matcher(trimmed).matches() || IPV6_PATTERN.matcher(trimmed).matches();
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
