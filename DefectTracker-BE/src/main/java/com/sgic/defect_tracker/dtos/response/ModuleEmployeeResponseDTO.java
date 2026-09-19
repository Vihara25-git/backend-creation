package com.sgic.defect_tracker.dtos.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleEmployeeResponseDTO {
    private Long id;
    private Long moduleId;
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isLeader;
}


