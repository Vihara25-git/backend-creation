package com.sgic.defect_tracker.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "defect_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "defect_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defectId", nullable = false)
    private Defect defect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id")
    private Employee assignedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private Employee assignedTo;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "defect_status")
    private String defectStatus;

    /**
     * Description of what changed.
     * Example:
     * "Defect created"
     * "Assignee changed"
     * "Status changed"
     */
    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private Employee updatedBy;

    @Column(name = "defect_date")
    private LocalDate defectDate;

    @Column(name = "defect_time")
    private LocalTime defectTime;
    @Column(name = "release_name")
    private String releaseName;
}