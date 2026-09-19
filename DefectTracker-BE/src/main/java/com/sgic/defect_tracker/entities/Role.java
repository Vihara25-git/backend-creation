package com.sgic.defect_tracker.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Role extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Column(nullable = false, unique = true)
    private String roleName;

    private String roleType;

    //Role <--> BenchAllocation
    @OneToMany(mappedBy = "role")
    @JsonIgnore
    private List<BenchAllocation> benchAllocations;


}