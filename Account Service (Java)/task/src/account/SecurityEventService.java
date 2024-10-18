package account;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SecurityEventService {

    private final HttpServletRequest request;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final SecurityEventRepository securityEventRepository;

    public SecurityEventService(HttpServletRequest request, ApplicationEventPublisher applicationEventPublisher, SecurityEventRepository securityEventRepository) {
        this.request = request;
        this.applicationEventPublisher = applicationEventPublisher;
        this.securityEventRepository = securityEventRepository;
    }

    @EventListener
    void handleSecurityEvent(SecurityEvent event) {
        securityEventRepository.save(event);
    }

    public void createEvent(Action action, String object) {
        var auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        createEvent(action, object, (User) auth);
    }
    public void createEvent(Action action, String object, String subject) {
        var e = new SecurityEvent();
        e.action = action;
        e.object = object;
        e.subject = subject;
        e.path = request.getRequestURI();
        e.date = LocalDate.now();
        applicationEventPublisher.publishEvent(e);
    }

    public void createEvent(Action action, String object, User subject) {
        createEvent(action, object, subject.email);
    }
}
