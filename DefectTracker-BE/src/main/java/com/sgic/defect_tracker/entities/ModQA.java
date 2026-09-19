package com.sgic.defect_tracker.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ModQA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modQaId;
    private Boolean isLeader = false;

    @ManyToOne()
    @JoinColumn(name="moduleId")
    private Module module;

    @ManyToOne()
    @JoinColumn(name = "empId")
    private Employee employee;


}