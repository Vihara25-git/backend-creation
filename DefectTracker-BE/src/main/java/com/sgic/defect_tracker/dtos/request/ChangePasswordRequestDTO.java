package com.sgic.defect_tracker.dtos.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {

    private Long empId;

    private String email;

    @NotBlank(message = "Current password is required")
    @JsonAlias({"oldPassword", "currentPassword"})
    private String currentPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;

    public void setOldPassword(String oldPassword) {
        if (this.currentPassword == null || this.currentPassword.isBlank()) {
            this.currentPassword = oldPassword;
        }
    }
}
