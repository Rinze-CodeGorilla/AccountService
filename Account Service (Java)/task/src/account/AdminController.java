package account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin")
public class AdminController {
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    AdminController(UserDetailsService userDetailsService, UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @PutMapping("user/role")
    UserResponse setRole(@Valid @RequestBody SetRoleRequest request) {
        User user = switch (request.operation) {
            case GRANT -> userDetailsService.grantRole(request.user, request.role);
            case REMOVE -> userDetailsService.removeRole(request.user, request.role);
        };
        return new UserResponse(user);

    }

    @DeleteMapping("user/{email}")
    UserDeletedResponse deleteUser(@PathVariable @Email(regexp = ".+@acme.com") @NotNull String email) {
        userDetailsService.delete(email);
        return new UserDeletedResponse(email);
    }

    @GetMapping("user/")
    List<UserResponse> listUsers() {
        return userDetailsService.findAll().stream().map(UserResponse::new).toList();
    }

    @PutMapping("user/access")
    ChangeAccessResult changeAccess(@RequestBody @Valid ChangeAccessRequest request) {
        User user = switch (request.operation) {
            case LOCK -> userDetailsService.lock(request.user);
            case UNLOCK -> userDetailsService.unlock(request.user);
        };
        return new ChangeAccessResult("User %s %s!".formatted(user.email, user.locked ? "locked" : "unlocked"));
    }

    enum ChangeAccessOperation {LOCK, UNLOCK}

    enum ChangeRoleOperation {GRANT, REMOVE}

    record ChangeAccessResult(String status) {
    }

    record ChangeAccessRequest(@Email String user, ChangeAccessOperation operation) {
    }

    record UserResponse(long id, String name, String lastname, String email, List<String> roles) {
        UserResponse(User user) {
            this(user.id, user.name, user.lastname, user.email, user.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted().toList());
        }
    }

    record UserDeletedResponse(String user, String status) {
        UserDeletedResponse(String email) {
            this(email, "Deleted successfully!");
        }
    }

    record SetRoleRequest(@Email @NotBlank String user, @NotBlank String role,
                          @NotNull AdminController.ChangeRoleOperation operation) {
    }
}
