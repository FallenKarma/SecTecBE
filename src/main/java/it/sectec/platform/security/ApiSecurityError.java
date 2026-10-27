package it.sectec.platform.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ApiSecurityError {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiSecurityError.class);
    private ApiSecurityError() {}

    public static void write(HttpServletResponse response, int status, String code) throws IOException {
        log.debug("Security response: status={} code={}", status, code);
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write("{\"code\":\"" + code + "\"}");
    }
}

