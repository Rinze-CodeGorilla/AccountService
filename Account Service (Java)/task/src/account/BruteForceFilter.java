package account;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class BruteForceFilter extends OncePerRequestFilter {
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        var principal = request.getUserPrincipal();
        if (principal instanceof Authentication authentication) {
            boolean isAuthenticated = authentication.isAuthenticated();
            User user = (User) authentication.getPrincipal();
            boolean isBlocked = isAuthenticated && user.locked;
            if (isBlocked) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "User account is locked");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
