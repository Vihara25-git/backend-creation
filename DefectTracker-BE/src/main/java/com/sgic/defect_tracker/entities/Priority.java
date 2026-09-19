package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Priority extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priorityId;

    private String priorityName;

    private String colorCode;

    @OneToMany(mappedBy = "priority")
    private List<Defect> defectList;
}
