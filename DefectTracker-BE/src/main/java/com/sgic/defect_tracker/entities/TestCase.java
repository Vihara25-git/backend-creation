package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class TestCase extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testCaseId;
    private Long testCaseNumber;
    private String testCaseName;
    private String description;
    private String testSteps;

    //TestCase <--> Project
    @ManyToOne
    @JoinColumn(name = "projectId")
    private ProjectDetails projectDetails;

    // TestCase <--> Module
    @ManyToOne
    @JoinColumn(name = "moduleId")
    private Module module;

    // TestCase <--> SubModule
    @ManyToOne
    @JoinColumn(name = "subModuleId")
    private SubModule subModule;

    // TestCase <--> Severity
    @ManyToOne
    @JoinColumn(name = "severityId")
    private Severity severity;

    // TestCase <--> Priority
    @ManyToOne
    @JoinColumn(name = "priorityId")
    private Priority priority;

    // TestCase <--> DefectType
    @ManyToOne
    @JoinColumn(name = "defectTypeId")
    private DefectType defectType;

    @OneToMany(mappedBy = "testCase")
    private List<ReleaseTestCase> releaseTestCases;

    @OneToOne(mappedBy = "testCase")
    private Defect defect;
}