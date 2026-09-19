package com.sgic.defect_tracker.dtos.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeverityResponseDTO {
    private Long severityId;

    private String severityName;

    private String colorCode;

    private Integer weight;
}
