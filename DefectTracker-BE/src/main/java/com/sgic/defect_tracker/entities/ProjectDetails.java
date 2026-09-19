package com.sgic.defect_tracker.entities;
import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Entity
@Data
public class ProjectDetails extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    private String projectName;
    private String projectDescription;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name =  "designation_id")
    private Designation designation;

    @ManyToOne (cascade =  CascadeType.ALL)
    @JoinColumn(name = "client_id")
    private ClientDetails clientDetails;



    @OneToMany(mappedBy = "project")
    private List<Module> modules;

    @ManyToOne
    @JoinColumn(name = "project_manager_id")
    private  Employee projectManager;

    @OneToMany(mappedBy = "projectDetails")
    private List<Defect> defectList;

    @OneToMany(mappedBy = "projectDetails")
    private List<BenchAllocation> benchAllocations;

    @OneToMany(mappedBy = "projectDetails")
    private List<TestCase> testCases;

    public void setDescription(Object description) {
    }



}