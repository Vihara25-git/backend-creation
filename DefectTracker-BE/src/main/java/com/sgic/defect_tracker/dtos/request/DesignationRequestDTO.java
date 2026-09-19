
package com.sgic.defect_tracker.dtos.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data

public class DesignationRequestDTO {
    @NotBlank(message = "Designation name is required")

    @Size(
            max = 25,
            message = "Designation name must not exceed 25 characters."
    )
    private String designationName;

}
