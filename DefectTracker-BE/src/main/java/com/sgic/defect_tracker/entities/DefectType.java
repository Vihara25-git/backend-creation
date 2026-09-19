package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class DefectType extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long defectTypeId;

    private String defectTypeName;

    @OneToMany(mappedBy = "defectType")
    private List<Defect> defectList;

    // DefectType <--> TestCase
    @OneToMany(mappedBy = "defectType")
    private List<TestCase> testCases;
}
