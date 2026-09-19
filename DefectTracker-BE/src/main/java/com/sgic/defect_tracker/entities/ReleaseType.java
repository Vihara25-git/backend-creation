package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class ReleaseType extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long releaseTypeId;

    @Column(name = "release_type_name", nullable = false)
    private String releaseTypeName;



    public void setType(@NotBlank(message = "Release type name is required") String type) {
        this.releaseTypeName = type;
    }

    public Long getId() {
        return this.releaseTypeId;
    }

    public String getType() {
        return this.releaseTypeName;
    }
}
