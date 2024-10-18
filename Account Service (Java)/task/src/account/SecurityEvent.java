package account;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class SecurityEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    LocalDate date;
    @Enumerated(EnumType.STRING)
    Action action;
    String subject;
    String object;
    String path;
}
