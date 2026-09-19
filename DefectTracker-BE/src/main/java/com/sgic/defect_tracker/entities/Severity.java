package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Severity extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long severityId;

    private String severityName;

    private String colorCode;

    @Column(unique = true, nullable = false)

    private Integer weight;

    @OneToMany(mappedBy = "severity")
    private List<Defect> defectList;

    // Severity <--> TestCase
    @OneToMany(mappedBy = "severity")
    private List<TestCase> testCases;
}
