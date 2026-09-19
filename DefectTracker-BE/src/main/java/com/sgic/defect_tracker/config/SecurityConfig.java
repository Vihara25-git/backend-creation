package com.sgic.defect_tracker.config;

import com.sgic.defect_tracker.service.EmployeeDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final EmployeeDetailsService employeeDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            EmployeeDetailsService employeeDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.employeeDetailsService = employeeDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            private final BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();

            @Override
            public String encode(CharSequence rawPassword) {
                if (rawPassword == null) {
                    return null;
                }
                return bCrypt.encode(rawPassword.toString().trim());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null || rawPassword == null) {
                    return false;
                }
                String raw = rawPassword.toString().trim();
                if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
                    try {
                        return bCrypt.matches(raw, encodedPassword);
                    } catch (Exception e) {
                        return false;
                    }
                }
                // Fallback for plaintext passwords
                return raw.equals(encodedPassword) || rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    // =========================================================
    // AUTHENTICATION PROVIDER
    // =========================================================

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(employeeDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }



    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                // =================================================
                // CORS
                // =================================================
                // Existing CorsConfig.java configuration use pannum
                .cors(cors -> {})

                // =================================================
                // CSRF
                // =================================================
                // JWT based application என்பதால் CSRF disable
                .csrf(csrf -> csrf.disable())

                // =================================================
                // SESSION
                // =================================================
                // JWT stateless authentication
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =================================================
                // AUTHORIZATION
                // =================================================
                .authorizeHttpRequests(auth -> auth

                        // -------------------------------------------------
                        // PUBLIC
                        // -------------------------------------------------

                        // Login & Auth
                        .requestMatchers("/api/v1/auth/**")
                        .permitAll()

                        // User profile & permissions
                        .requestMatchers("/api/v1/user/me/**")
                        .authenticated()

                        // -------------------------------------------------
                        // PRIVILEGE CONFIGURATION
                        // -------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/permission",
                                "/api/v1/assign-permission/**",
                                "/api/v1/employee/*/permission",
                                "/api/v1/privilege-templates/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "PERMISSION_READ", "ROLE_PERMISSION_READ", "EMPLOYEE_PERMISSION_READ")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/assign-permission/**",
                                "/api/v1/employee/*/permission",
                                "/api/v1/privilege-templates/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_PERMISSION_ASSIGN", "EMPLOYEE_PERMISSION_ASSIGN")

                        // -------------------------------------------------
                        // EMPLOYEE
                        // -------------------------------------------------

                        // Create Employee
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/Employee/createEmployee"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "EMPLOYEE_CREATE")

                        // Update Employee
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/Employee/update/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "EMPLOYEE_UPDATE")

                        // Delete Employee
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/Employee/DeleteEmployee/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "EMPLOYEE_DELETE")

                        // Update Employee Status
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/Employee/status/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "EMPLOYEE_STATUS_UPDATE", "EMPLOYEE_UPDATE")

                        // View Employees
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/Employee/**"
                        )
                        .hasAnyAuthority(
                                "ROLE_ADMIN",
                                "EMPLOYEE_READ"
                        )

                        // -------------------------------------------------
                        // DEFECT
                        // -------------------------------------------------
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/defect/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "DEFECT_CREATE")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/defect/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "DEFECT_UPDATE")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/defect/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "DEFECT_DELETE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/defect/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "DEFECT_READ")

                        // -------------------------------------------------
                        // PROJECT
                        // -------------------------------------------------
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/project/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "PROJECT_CREATE")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/project/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "PROJECT_UPDATE")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/project/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "PROJECT_DELETE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/project/**"
                        )
                        .hasAnyAuthority("ROLE_ADMIN", "PROJECT_READ")

                        // -------------------------------------------------
                        // EVERYTHING ELSE
                        // -------------------------------------------------

                        // Other APIs require authentication
                        .anyRequest()
                        .authenticated()
                )

                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}