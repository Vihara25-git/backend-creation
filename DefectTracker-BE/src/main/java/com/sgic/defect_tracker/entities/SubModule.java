package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class SubModule extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subModuleId;

    private String subModuleName;
    private Long submoduleDevId;



    //SubModule <--> Module
    @ManyToOne
    @JoinColumn(name = "moduleId")
    private Module module;

    //SubModule<--> Defect
    @OneToMany(mappedBy = "subModule")
    private List<Defect> defects;

    //Submodule <--> SubmoduleDev
    @OneToMany(mappedBy = "subModule")
    private List<SubmoduleDev> submoduleDevs;

//    //SubModule <--> Module
//    @ManyToOne
//    @JoinColumn(name = "moduleId")
//    private Module module;

    // SubModule <--> TestCase
    @OneToMany(mappedBy = "subModule")
    private List<TestCase> testCases;

}
