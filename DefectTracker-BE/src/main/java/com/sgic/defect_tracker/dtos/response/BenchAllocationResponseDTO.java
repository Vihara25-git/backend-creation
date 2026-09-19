package com.sgic.defect_tracker.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.ProjectDetails;
import com.sgic.defect_tracker.entities.Role;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDateTime;

@Data
public class BenchAllocationResponseDTO {

    private Long benchId;
    private Long availability;
    private Long designationId;
    private String designationName;
    private String availablePeriod;

    private Long empId;


    private String firstName;
    private String lastName;

    private String employeeName;
    private String employeeEmail;

    private Long projectId;
    private String projectName;

    private Long roleId;
    private String roleType; // update my module
    private String roleName;
    private Long benchAllocationId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;





}
