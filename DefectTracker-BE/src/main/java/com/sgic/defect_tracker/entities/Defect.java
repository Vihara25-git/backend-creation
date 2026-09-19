package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data

@Table(
        name = "defect",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "projectId",
                                "project_defect_number"
                        }
                )
        }
)
public class Defect extends DateAudit {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long defectId;

        private String briefDescription;

        private String steps;

        private String attachmentImage;

        private Boolean testCaseRequired;

        private String enterBy;


        @ManyToOne()
        @JoinColumn(name="moduleId")
        private Module module;

        @ManyToOne()
        @JoinColumn(name="subModuleId")
        private SubModule subModule;

        @ManyToOne()
        @JoinColumn(name="defectTypeId")
        private DefectType defectType;

        @ManyToMany()
        @JoinTable(name = "DefectReleaseVersion",
                joinColumns = @JoinColumn(name="defectId"),
                inverseJoinColumns = @JoinColumn(name="releaseId"))
        private List<ReleaseView> releaseViews;

        @ManyToOne()
        @JoinColumn(name="severityId")
        private Severity severity;


        @ManyToOne()
        @JoinColumn(name = "priorityId")
        private Priority priority;

        @ManyToOne()
        @JoinColumn(name = "statusTypeId")
        private StatusType statusType;

        @ManyToOne()

        @JoinColumn(name="projectId")
        private ProjectDetails projectDetails;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "testCaseId")
    private TestCase testCase;
//
//        @OneToMany(mappedBy = "defect")
//        private List<SubDev> subDev;

        @OneToMany(mappedBy = "defect")
        private List<SubmoduleDev> subDev;


        @ManyToOne()
    @JoinColumn(name = "assignToId")
    private Employee assignTo;

    @OneToMany(
            mappedBy = "defect",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Comment> comments;


    @OneToMany(
            mappedBy = "defect",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DefectHistory> defectHistories;

    @Column(name = "project_defect_number", nullable = false)
    private Long projectDefectNumber;
    }






