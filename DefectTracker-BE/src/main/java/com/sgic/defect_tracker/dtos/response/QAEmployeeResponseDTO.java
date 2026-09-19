package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

@Data
public class QAEmployeeResponseDTO {

    private Long benchAllocationId;

    private Long empId;

    private String firstName;

    private String lastName;

    private String email;

    private String roleType;
}