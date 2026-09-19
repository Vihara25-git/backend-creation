package com.sgic.defect_tracker.dtos.request;

import com.sgic.defect_tracker.entities.ReleaseType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;


@Data
@Getter
@Setter

public class ReleaseTypeRequestDTO {

    @NotBlank(message = "Release type name is required")
    private String type;

    Optional<ReleaseType> findByReleaseTypeNameIgnoreCase(String releaseTypeName) {
        return null;
    }

    boolean existsByReleaseTypeNameIgnoreCase(String releaseTypeName) {
        return false;
    }

}
