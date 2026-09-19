package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRequestDTO {

    private String firstName;
    private String lastName;
    private String gender;
    private Long designationId;
    private String whatsappNumber;
    private String email;
    private LocalDate joinDate;
    private String password;
}