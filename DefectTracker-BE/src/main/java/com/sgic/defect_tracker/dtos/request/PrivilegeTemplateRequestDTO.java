package com.sgic.defect_tracker.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrivilegeTemplateRequestDTO {

    @NotBlank(message = "Module/Type cannot be blank")
    private String type;

    @NotBlank(message = "Action/SubType cannot be blank")
    private String subType;

    private String description;
}
