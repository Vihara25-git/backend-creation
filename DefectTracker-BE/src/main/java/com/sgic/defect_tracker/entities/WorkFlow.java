package com.sgic.defect_tracker.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class WorkFlow extends DateAudit {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workflowId;

    @Column(name="status_type_id1")
    private Long statusTypeId1;

    @Column(name="status_type_id2")
    private Long statusTypeId2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "status_type_id1",
            insertable = false,
            updatable = false
    )
    private StatusType fromStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "status_type_id2",
            insertable = false,
            updatable = false
    )
    private StatusType toStatus;

}
