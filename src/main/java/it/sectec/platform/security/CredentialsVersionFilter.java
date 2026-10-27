package it.sectec.platform.security;

import it.sectec.platform.identity.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class CredentialsVersionFilter extends OncePerRequestFilter {
    private final UserRepository users;
    public CredentialsVersionFilter(UserRepository users) { this.users = users; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AccountPrincipal principal) {
            var user = users.findById(principal.profile().id());
            if (user.isEmpty() || !"ACTIVE".equals(user.get().getStatus()) ||
                    user.get().getCredentialsVersion() != principal.credentialsVersion()) {
                var session = request.getSession(false);
                if (session != null) session.invalidate();
                SecurityContextHolder.clearContext();
                ApiSecurityError.write(response, 401, "SESSION_REVOKED");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}

