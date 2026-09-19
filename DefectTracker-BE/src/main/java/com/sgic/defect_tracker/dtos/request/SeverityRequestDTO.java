package com.sgic.defect_tracker.dtos.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class SeverityRequestDTO {
    //  private Long severityId;
    @Size(max = 20, message = "Module name must be between 3 and 20 characters")
    private String severityName;

    private String colorCode;

    @Column(unique = true)
    private Integer weight;
}

