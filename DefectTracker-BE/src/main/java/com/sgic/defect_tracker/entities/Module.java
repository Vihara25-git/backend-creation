package com.sgic.defect_tracker.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Module extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long moduleId;

    private String moduleName;

    //Module <--> Defect
    @OneToMany(mappedBy = "module")
    private List<Defect> defects;

    //Module <--> SubModule
    @OneToMany(mappedBy = "module")
    @JsonIgnore
    private  List<SubModule> subModule;

    //Module <--> Project
    @ManyToOne
    @JoinColumn(name = "projectId")
    private ProjectDetails project;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
    private List<ModQA> modQAS;

    // Module <--> TestCase
    @OneToMany(mappedBy = "module")
    private List<TestCase> testCases;


}
