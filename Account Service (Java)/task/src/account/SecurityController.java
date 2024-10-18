package account;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/security")
public class SecurityController {

    private final SecurityEventRepository securityEventRepository;

    SecurityController(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    @GetMapping("events/")
    List<SecurityEventResponse> listEvents() {
        return securityEventRepository.findAll().stream().map(SecurityEventResponse::new).toList();
    }

    record SecurityEventResponse(long id, LocalDate date, Action action, String subject, String object, String path) {
        SecurityEventResponse(SecurityEvent e) {
            this(e.id, e.date, e.action, e.subject == null ? "Anonymous" : e.subject, e.object, e.path);
        }
    }
}
