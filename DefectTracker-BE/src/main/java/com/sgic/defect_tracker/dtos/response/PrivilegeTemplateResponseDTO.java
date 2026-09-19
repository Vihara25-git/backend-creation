package com.sgic.defect_tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeTemplateResponseDTO {

    private Long id;
    private String type;
    private String subType;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
