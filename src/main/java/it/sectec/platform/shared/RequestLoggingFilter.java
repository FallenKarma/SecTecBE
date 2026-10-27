package it.sectec.platform.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

/** Logs route templates only: raw URLs, query strings and bodies may contain secrets. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String previous = MDC.get("requestId");
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);
        long started = System.nanoTime();
        boolean failed = false;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            failed = true;
            // Exception messages and nested causes can contain SQL values or credentials.
            log.error("Unhandled request failure: exceptionType={}", ex.getClass().getName());
            for (StackTraceElement frame : ex.getStackTrace()) {
                if (frame.getClassName().startsWith("it.sectec.")) log.error("Application frame: {}", frame);
            }
            throw ex;
        } finally {
            try {
                Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                String method = switch (request.getMethod()) {
                    case "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD" -> request.getMethod();
                    default -> "OTHER";
                };
                int status = failed ? 500 : response.getStatus();
                var event = status >= 500 ? log.atError() : status >= 400 ? log.atWarn() : log.atDebug();
                event.log("HTTP completed: method={} route={} status={} durationMs={} outcome={}",
                        method, route == null ? "<unmapped-or-filtered>" : route, status,
                        (System.nanoTime() - started) / 1_000_000, failed ? "EXCEPTION" : "RESPONSE");
            } finally {
                if (previous == null) MDC.remove("requestId"); else MDC.put("requestId", previous);
            }
        }
    }
}
