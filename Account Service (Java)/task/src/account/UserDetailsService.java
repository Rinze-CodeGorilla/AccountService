package account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityEventService securityEventService;

    UserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder, SecurityEventService securityEventService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityEventService = securityEventService;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(username);
        if (user == null) throw new UsernameNotFoundException(username);
        return user;
    }

    public User createUser(String name, String lastname, String email, String password) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("User exist!");
        }
        validatePassword(password);
        Role role = Role.USER;
        if (!userRepository.existsBy()) role = Role.ADMINISTRATOR;
        User user = userRepository.save(new User(name, lastname, email.toLowerCase(), passwordEncoder.encode(password), Set.of(role)));
        securityEventService.createEvent(Action.CREATE_USER, user.email, "Anonymous");
        return user;
    }

    public void updatePassword(User user, @NotBlank String newPassword) {
        validateNewPassword(newPassword, user.getPassword());
        user.password = passwordEncoder.encode(newPassword);
        securityEventService.createEvent(Action.CHANGE_PASSWORD, user.email);
        userRepository.save(user);
    }

    private void validateNewPassword(String newPassword, String oldPassword) {
        if (passwordEncoder.matches(newPassword, oldPassword))
            throw new PasswordException("The passwords must be different!");
        validatePassword(newPassword);
    }

    private void validatePassword(String password) {
        if (password.length() < 12) throw new PasswordException("Password length must be 12 chars minimum!");
        String[] breachedPasswords = {"PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"};
        if (Arrays.asList(breachedPasswords).contains(password))
            throw new PasswordException("The password is in the hacker's database!");
    }

    public void delete(@Email(regexp = ".+@acme.com") @NotNull String email) {
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) throw new NotFoundException("User not found!");
        if (user.roles.contains(Role.ADMINISTRATOR)) throw new BadRequestException("Can't remove ADMINISTRATOR role!");

        securityEventService.createEvent(Action.DELETE_USER, user.email);
        userRepository.delete(user);
    }

    public User grantRole(String email, String roleName) {
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) throw new NotFoundException("User not found!");
        Role role;
        try {
            role = Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Role not found!");
        }
        if (user.roles.contains(Role.ADMINISTRATOR) && role != Role.ADMINISTRATOR)
            throw new BadRequestException("The user cannot combine administrative and business roles!");
        if (role == Role.ADMINISTRATOR && !user.roles.contains(Role.ADMINISTRATOR))
            throw new BadRequestException("The user cannot combine administrative and business roles!");
        user.roles.add(role);
        securityEventService.createEvent(Action.GRANT_ROLE, "Grant role %s to %s".formatted(role.name(), user.email));
        return userRepository.save(user);
    }

    public User removeRole(String email, String roleName) {
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) throw new NotFoundException("User not found!");
        Role role;
        try {
            role = Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Role not found!");
        }
        if (!user.roles.contains(role)) throw new BadRequestException("The user does not have a role!");
        if (role == Role.ADMINISTRATOR) throw new BadRequestException("Can't remove ADMINISTRATOR role!");
        if (user.roles.size() <= 1) throw new BadRequestException("The user must have at least one role!");
        user.roles.remove(role);
        securityEventService.createEvent(Action.REMOVE_ROLE, "Remove role %s from %s".formatted(role.name(), user.email));
        return userRepository.save(user);
    }

    public User lock(String email) {
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) throw new NotFoundException("User not found!");
        if (user.roles.contains(Role.ADMINISTRATOR)) {
            throw new CantLockAdministratorException();
        }
        user.locked = true;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String subject = authentication == null ? user.email : authentication.getPrincipal().toString();
        securityEventService.createEvent(Action.LOCK_USER, "Lock user %s".formatted(user.email), subject);
        return userRepository.save(user);
    }

    public User unlock(String email) {
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) throw new NotFoundException("User not found!");
        user.locked = false;
        securityEventService.createEvent(Action.UNLOCK_USER, "Unlock user %s".formatted(user.email));
        return userRepository.save(user);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class ConflictException extends RuntimeException {
        ConflictException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class PasswordException extends RuntimeException {
        public PasswordException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class NotFoundException extends RuntimeException {
        NotFoundException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class CantLockAdministratorException extends RuntimeException {
        public CantLockAdministratorException() {
            super("Can't lock the ADMINISTRATOR!");
        }
    }
}
