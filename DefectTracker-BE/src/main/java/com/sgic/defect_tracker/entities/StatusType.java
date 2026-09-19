package com.sgic.defect_tracker.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class StatusType extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusTypeId;

    private String statusName;

    private String statusType;

    private String colorCode;

    @OneToMany(mappedBy = "fromStatus")
    private List<WorkFlow> fromWorkFlows;

    @OneToMany(mappedBy = "toStatus")
    private List<WorkFlow> toWorkFlows;
    //StatusType <--> WorkFlow

    @OneToMany(mappedBy = "statusType")
    private List<Defect> defect;

}
