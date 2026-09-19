package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDTO {
    private Long empId;
    private String firstName;
    private String lastName;
    private String gender;
    private String status;
    private String whatsappNumber;
    private String email;
    private LocalDate joinDate;

    private String password;

    private Boolean isActive;

    private Long designationId;
    private String designationName;
    private Long availablePercentage;
}