package com.sgic.defect_tracker.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
public class BenchAllocation extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long benchAllocationId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // LocalDateTime
    private Long availability;

    //BenchAllocation <--> Employee
//    @ManyToOne
//    @JoinColumn(name = "empId")
//    private Employee employee;

//    @ManyToOne
//    @JoinColumn(
//            name = "empId",
//            foreignKey = @ForeignKey(
//                    name = "fk_bench_employee",
//                    foreignKeyDefinition =
//                            "FOREIGN KEY (empId) REFERENCES employee(emp_id) ON DELETE SET NULL"
//            )
//    )
//    private Employee employee;

    @ManyToOne(optional = true)
    @JoinColumn(
            name = "empId",
            nullable = true,
            foreignKey = @ForeignKey(name = "fk_bench_employee")
    )
    private Employee employee;

    private String employeeName;
    private String employeeEmail;
    //BenchAllocation <--> Role
    @ManyToOne
    @JoinColumn(name = "roleId")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "projectId")
    @JsonIgnore
    private ProjectDetails projectDetails;

    @OneToMany(mappedBy = "benchAllocation")
    private List<ReleaseTestCase> releaseTestCases;
}