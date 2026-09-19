package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.LoginRequestDTO;
import com.sgic.defect_tracker.dtos.response.LoginResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    // forget password

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    boolean validateResetToken(String token);
}