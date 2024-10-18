package account;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class SpringSecurityConfig {
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final BruteForceFilter bruteForceFilter;

    SpringSecurityConfig(RestAuthenticationEntryPoint restAuthenticationEntryPoint, CustomAccessDeniedHandler customAccessDeniedHandler, BruteForceFilter bruteForceFilter) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.bruteForceFilter = bruteForceFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                ) // Handle auth errors
                .csrf(csrf -> csrf.disable()) // For Postman
                .headers(headers -> headers.frameOptions().disable()) // For the H2 console
                .addFilterBefore(bruteForceFilter, AuthorizationFilter.class)
                .authorizeHttpRequests(auth -> auth  // manage access
                                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/changepass").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyRole(Role.ACCOUNTANT.name(), Role.USER.name())
                                .requestMatchers(HttpMethod.POST, "/api/acct/payments").hasRole(Role.ACCOUNTANT.name())
                                .requestMatchers(HttpMethod.PUT, "/api/acct/payments").hasRole(Role.ACCOUNTANT.name())
                                .requestMatchers(HttpMethod.GET, "/api/admin/user/").hasRole(Role.ADMINISTRATOR.name())
                                .requestMatchers(HttpMethod.DELETE, "/api/admin/user/{email}").hasRole(Role.ADMINISTRATOR.name())
                                .requestMatchers(HttpMethod.PUT, "/api/admin/user/role").hasRole(Role.ADMINISTRATOR.name())
                                .requestMatchers(HttpMethod.PUT, "/api/admin/user/access").hasRole(Role.ADMINISTRATOR.name())
                                .requestMatchers(HttpMethod.GET, "/api/security/events/").hasRole(Role.AUDITOR.name())
                                .requestMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()
                                .requestMatchers("/error").permitAll()
                                .anyRequest().denyAll()
                        // other matchers
                )
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                );

        return http.build();
    }
}
