package com.sgic.defect_tracker.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sgic.defect_tracker.dtos.request.LoginRequestDTO;
import com.sgic.defect_tracker.dtos.response.LoginResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.AuthService;
import com.sgic.defect_tracker.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sgic.defect_tracker.dtos.request.ChangePasswordRequestDTO;
import com.sgic.defect_tracker.dtos.request.LoginRequestDTO;
import com.sgic.defect_tracker.dtos.response.LoginResponseDTO;
import com.sgic.defect_tracker.dtos.request.ChangePasswordRequestDTO;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.sgic.defect_tracker.dtos.request.ChangePasswordRequestDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



import static com.sgic.defect_tracker.utils.EndpointBundle.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmployeeService employeeService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO loginRequestDTO) {

        return ResponseEntity.ok(
                authService.login(loginRequestDTO)
        );
    }

    @PostMapping(EndpointBundle.CHANGE_PASSWORD)
    public ResponseEntity<ResponseWrapper<Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO requestDTO) {

        employeeService.changePassword(requestDTO);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Password changed successfully",
                        null
                )
        );
    }

    @PostMapping({EndpointBundle.LOG_OUT, EndpointBundle.LOGOUT})
    public ResponseEntity<ResponseWrapper<Object>> logout() {
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Logged out successfully",
                        null
                )
        );
    }

    @PostMapping({FORGET_PASSWORD, "/forgot-password"})
    public ResponseEntity<ResponseWrapper<Object>> forgotPassword(
            @RequestParam(required = false) String email,
            @RequestBody(required = false) java.util.Map<String, String> body) {

        String targetEmail = email;
        if ((targetEmail == null || targetEmail.isBlank()) && body != null) {
            targetEmail = body.get("email");
        }
        if (targetEmail == null || targetEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        authService.forgotPassword(targetEmail.trim());

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Password reset link has been sent to your email.",
                        null
                )
        );
    }

    @PostMapping({RESET_PASSWORD, "/resetpassword"})
    public ResponseEntity<ResponseWrapper<Object>> resetPassword(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String newPassword,
            @RequestBody(required = false) java.util.Map<String, String> body) {

        String targetToken = token;
        String targetNewPassword = newPassword;

        if ((targetToken == null || targetToken.isBlank()) && body != null) {
            targetToken = body.get("token");
        }
        if ((targetNewPassword == null || targetNewPassword.isBlank()) && body != null) {
            targetNewPassword = body.get("newPassword");
        }

        if (targetToken == null || targetToken.isBlank()) {
            throw new IllegalArgumentException("Reset token is required");
        }
        if (targetNewPassword == null || targetNewPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        authService.resetPassword(targetToken.trim(), targetNewPassword.trim());

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Password has been reset successfully.",
                        null
                )
        );
    }

    @GetMapping(VALIDATE_TOKEN)
    public ResponseEntity<ResponseWrapper<Object>> validateResetToken(
            @RequestParam String token) {

        authService.validateResetToken(token);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Password reset link is valid.",
                        null
                )
        );
    }
}
