package com.sgic.defect_tracker.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Designation extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long designationId;

    private String designationName;

    //Designation <--> Employee
    @OneToMany(mappedBy = "designation")
    @JsonIgnore
    private List<Employee> employee;
}