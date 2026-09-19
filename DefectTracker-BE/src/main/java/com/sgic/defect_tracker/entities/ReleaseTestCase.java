package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import com.sgic.defect_tracker.utils.ReleaseStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ReleaseTestCase extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long releaseTestCaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "releaseId", nullable = false)
    private ReleaseView releaseView;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "testCaseId", nullable = false)
    private TestCase testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benchAllocationId")
    private BenchAllocation benchAllocation;

    @Column(name = "pass_or_fail")
    private String passOrFail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReleaseStatus status = ReleaseStatus.ACTIVE;
}