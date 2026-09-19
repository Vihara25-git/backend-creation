package com.sgic.defect_tracker.dtos.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String message;
    private String token;
    private Long employeeId;
    private String employeeName;
    private String email;
    private List<String> roles;

    public LoginResponseDTO(
            String message,
            String token,
            Long employeeId,
            String employeeName,
            List<String> roles
    ) {
        this.message = message;
        this.token = token;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.roles = roles;
    }
}