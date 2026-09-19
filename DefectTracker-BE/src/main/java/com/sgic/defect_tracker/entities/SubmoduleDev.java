package com.sgic.defect_tracker.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class SubmoduleDev extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subModuledevId;


    @ManyToOne()
    @JoinColumn(name = "subModuleId")
    @JsonIgnore
    private SubModule subModule;

    @ManyToOne()
    @JoinColumn(name = "emp")
    private Employee employee;

    @ManyToOne()
    @JoinColumn(name = "defectId")
    private Defect defect;

   // private Long empId;


//    // SubModuleDev <--> Employee
//    @OneToMany(mappedBy = "submoduleDev")
//    private List<Employee> employees;


}
