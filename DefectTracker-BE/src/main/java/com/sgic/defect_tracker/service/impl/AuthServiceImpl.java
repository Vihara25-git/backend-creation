package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.LoginRequestDTO;
import com.sgic.defect_tracker.dtos.response.LoginResponseDTO;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.service.AuthService;
import com.sgic.defect_tracker.service.JwtTokenService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.service.EmailNotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationProvider authenticationProvider;
    private final JwtTokenService jwtTokenService;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {

        if (loginRequestDTO == null || loginRequestDTO.getEmail() == null || loginRequestDTO.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }

        String email = loginRequestDTO.getEmail().trim();
        String password = loginRequestDTO.getPassword().trim();

        authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );

        Employee employee = employeeRepository.findByEmailIgnoreCase(
                email
        ).orElseThrow(() ->
                new UsernameNotFoundException("Employee not found")
        );

        String fullName = (employee.getFirstName() != null ? employee.getFirstName() : "") +
                (employee.getLastName() != null && !employee.getLastName().isBlank() ? " " + employee.getLastName() : "");

        return new LoginResponseDTO(
                "Login successful",
                jwtTokenService.generateToken(employee.getEmail()),
                employee.getEmpId(),
                fullName.trim(),
                employee.getEmail(),
                employee.getRole() != null
                        ? List.of(employee.getRole().getRoleName())
                        : List.of()
        );
    }

    private final EmailNotificationService notificationService;

    @Value("${app.frontend.url}")
    private String frontendUrl;
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@#$!%*?&]{8,}$";
    @Override
    @Transactional
    public void forgotPassword(String email) {

        // 1. Find employee by email
        String cleanEmail = email != null ? email.trim() : "";
        Employee employee = employeeRepository.findByEmailIgnoreCase(cleanEmail)
                .or(() -> employeeRepository.findByEmail(cleanEmail))
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No account found with this email address."
                        )
                );

        // 2. Generate random reset token
        String token = UUID.randomUUID().toString();

        // 3. Token expiry = 10 minutes from now
        LocalDateTime expiryTime =
                LocalDateTime.now().plusMinutes(10);

        // 4. Save token and expiry in Employee table
        employee.setForgetPasswordToken(token);
        employee.setForgetPasswordTokenExpiry(expiryTime);

        employeeRepository.save(employee);

        // 5. Create frontend reset password link
        String resetLink =
                frontendUrl + "/reset-password?token=" + token;

        // 6. Prepare email template variables
        Map<String, Object> variables = Map.of(
                "employeeName",
                employee.getFirstName() + " " + employee.getLastName(),

                "resetLink",
                resetLink
        );

        // 7. Send password reset email
        notificationService.sendEmail(
                "PASSWORD_RESET",
                employee.getEmail(),
                variables
        );
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {


        Employee employee = employeeRepository
                .findByForgetPasswordToken(token)
                .orElseThrow(() ->
                        new RuntimeException("Invalid or expired password reset link")
                );

        if (employee.getForgetPasswordTokenExpiry() == null ||
                employee.getForgetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {

            throw new RuntimeException("Password reset link has expired");
        }
        // Password validation
        String passwordPattern =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@#$!%*?&]{8,}$";

        if (newPassword == null || !newPassword.matches(passwordPattern)) {
            throw new RuntimeException(
                    "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character."
            );
        }
        String encodedPassword = passwordEncoder != null
                ? passwordEncoder.encode(newPassword.trim())
                : newPassword.trim();
        employee.setPassword(encodedPassword);

        // Clear token after successful password reset
        employee.setForgetPasswordToken(null);
        employee.setForgetPasswordTokenExpiry(null);

        employeeRepository.save(employee);

        try {
            String fullName = (employee.getFirstName() != null ? employee.getFirstName() : "") +
                    (employee.getLastName() != null && !employee.getLastName().isBlank() ? " " + employee.getLastName() : "");

            Map<String, Object> variables = Map.of(
                    "employeeName", fullName.trim().isEmpty() ? "User" : fullName.trim()
            );

            notificationService.sendEmail(
                    "PASSWORD_RESET_SUCCESS",
                    employee.getEmail(),
                    variables
            );
        } catch (Exception e) {
            System.err.println("Could not send PASSWORD_RESET_SUCCESS email: " + e.getMessage());
        }
    }

    @Override
    public boolean validateResetToken(String token) {

        Employee employee = employeeRepository
                .findByForgetPasswordToken(token)
                .orElseThrow(() ->
                        new RuntimeException("Invalid password reset link")
                );

        if (employee.getForgetPasswordTokenExpiry() == null ||
                employee.getForgetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {

            throw new RuntimeException("Password reset link has expired");
        }

        return true;
    }
}