package account;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final UserDetailsService userDetailsService;

    AuthController(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("signup")
    @PermitAll
    public AdminController.UserResponse signup(@Valid @RequestBody SignupRequest request) {
        User user = userDetailsService.createUser(request.name, request.lastname, request.email, request.password);
        return new AdminController.UserResponse(user);
    }

    @PostMapping("changepass")
    @PreAuthorize("isAuthenticated()")
    public ChangePasswordResponse changePassword(@Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal User user) {
        userDetailsService.updatePassword(user, request.new_password);
        return new ChangePasswordResponse(user.email);
    }

    record SignupRequest(@NotBlank String name, @NotBlank String lastname,
                         @Email(regexp = ".+@acme.com") @NotNull String email, @NotNull String password) {
    }

    record ChangePasswordRequest(@NotNull String new_password) {
    }

    record ChangePasswordResponse(String email, String status) {
        ChangePasswordResponse(String email) {
            this(email, "The password has been updated successfully");
        }
    }
}
