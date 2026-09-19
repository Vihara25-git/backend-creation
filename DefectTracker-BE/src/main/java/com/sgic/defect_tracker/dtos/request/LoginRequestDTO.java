package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

@Data
public class LoginRequestDTO {

    private String email;
    private String password;
}