package com.sgic.defect_tracker.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data

public class DefectRequestDTO {

    @Pattern(
            regexp = "^(?=.*[a-zA-Z]).+$",
            message = "Must contain at least one letter"
    )
    private String briefDescription;

    @Pattern(
            regexp = "(?s).*\\p{L}.*",
            message = "Must contain at least one letter"
    )
    private String steps;


    private String attachmentImage;

    private Boolean testCaseRequired;
    private Boolean isAddTestCase;

    public Boolean getTestCaseRequired() {
        if (testCaseRequired != null) {
            return testCaseRequired;
        }
        return isAddTestCase;
    }

    private Long testCaseId;

//@NotNull(message="module need select")
     private Long moduleId;
   // private String moduleName;

//  @NotNull(message="submodule need select")
     private Long subModuleId;

    //private String subModuleName;

//  @NotNull(message="defect type need select")
    private Long defectTypeId;
    //private Long defectTypeName;

// @NotNull(message="Release need select")
////     private Long releaseId;
// @NotNull(message="Release need select")
 private List<Long> releaseIds;
    //private String releaseName;
//    @NotNull(message="Severity")
     private Long severityId;
    //private String severityName;

//    @NotNull(message="priority")
    private Long priorityId;

    //private String priorityName;

//    @NotNull(message="Status Type need select")
    private Long statusTypeId;

    //private String statusName;
//    @NotNull(message="project not selected please check")
    private Long projectId;
//   @NotNull(message="submodule need select")
    private Long subDevId;

    // private String projectName;
//   @NotBlank(message="need select assignee")
    private String enterBy;

//       @NotBlank(message="need select assignee")
    private Long assignToId;
    private Boolean removeAttachment;
}
