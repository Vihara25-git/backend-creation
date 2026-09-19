package com.sgic.defect_tracker.dtos.request;

import com.sgic.defect_tracker.utils.ReleaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TestCaseRequestDTO {

    @NotBlank(message = "Test Case Name is required")
    @Size(max = 255, message = "Test Case Name must not exceed 255 characters")
    private String testCaseName;

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Test Steps are required")
    @Size(max = 255, message = "Test Steps must not exceed 5000 characters")
    private String testSteps;

    @NotNull(message = "Defect Type is required")
    private Long defectTypeId;

    @NotNull(message = "Project is required")
    private Long projectId;

    @NotNull(message = "Module is required")
    private Long moduleId;

    @NotNull(message = "Sub Module is required")
    private Long subModuleId;

    @NotNull(message = "Severity is required")
    private Long severityId;

    private ReleaseStatus status;
}