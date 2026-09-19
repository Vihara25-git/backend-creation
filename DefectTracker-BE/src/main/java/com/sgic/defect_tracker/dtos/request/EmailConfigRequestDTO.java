package com.sgic.defect_tracker.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailConfigRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "From email is required")
    private String fromEmail;

    private String fromName;

    private String host;

    private Integer port;

    private Boolean status;

    private String smtpUserName;

    private String smtpPassword;
}