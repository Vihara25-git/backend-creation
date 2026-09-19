package com.sgic.defect_tracker.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateKlocRequest {

    @NotNull(message = "KLOC is required")
    @DecimalMin(
            value = "0.1",
            inclusive = true,
            message = "KLOC must be greater than or equal to 0.1"
    )
    private Double kloc;
}