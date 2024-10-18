package account;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SpringSecurityEventHandler {
    private final SecurityEventService eventService;
    private final BruteForceCounter bruteForceCounter;
    private final UserDetailsService userDetailsService;
    private final HttpServletRequest request;

    SpringSecurityEventHandler(SecurityEventService eventService, BruteForceCounter bruteForceCounter, UserDetailsService userDetailsService, HttpServletRequest request) {
        this.eventService = eventService;
        this.bruteForceCounter = bruteForceCounter;
        this.userDetailsService = userDetailsService;
        this.request = request;
    }

    @EventListener
    void handleAuthorizationDenied(AuthorizationDeniedEvent event) {
        String email = event.getAuthentication().get().getPrincipal().toString();
        User user = userDetailsService.loadUserByUsername(email);
        eventService.createEvent(Action.ACCESS_DENIED, request.getRequestURI(), user);
    }

    @EventListener
    void handleBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
        String email = event.getAuthentication().getPrincipal().toString();
        eventService.createEvent(Action.LOGIN_FAILED, request.getRequestURI(), email);
        bruteForceCounter.increment(email);
        if (bruteForceCounter.isBlocked(email)) {
            eventService.createEvent(Action.BRUTE_FORCE, request.getRequestURI(), email);
            try {
                userDetailsService.lock(email);
            } catch (UserDetailsService.CantLockAdministratorException ignored) {
                // don't lock administrator to prevent inaccessible system
            }
        }
    }

    @EventListener
    void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        bruteForceCounter.reset(event.getAuthentication().getPrincipal().toString());
    }

    @EventListener
    void handleAuthenticationFailureLockedEvent(AuthenticationFailureLockedEvent event) throws IOException {
        //not using this because I cannot customize the error message, it always sends Unauthorized as message, while Hyperskill requires a specific message
    }
}
