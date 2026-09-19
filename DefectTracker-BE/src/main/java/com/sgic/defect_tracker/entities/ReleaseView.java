package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import com.sgic.defect_tracker.utils.ReleaseStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class ReleaseView extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long releaseId;

    private String releaseName;

    private String releaseVersion;

    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReleaseStatus status = ReleaseStatus.ACTIVE;

    @ManyToMany(mappedBy = "releaseViews")
    private List<Defect> defectList;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectDetails projectDetails;

    @ManyToOne
    @JoinColumn(name = "release_type_id")
    private ReleaseType releaseType;

    @OneToMany(mappedBy = "releaseView")
    private List<ReleaseTestCase> releaseTestCases;

}
