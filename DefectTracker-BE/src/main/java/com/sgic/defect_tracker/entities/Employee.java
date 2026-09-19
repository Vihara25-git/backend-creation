package com.sgic.defect_tracker.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sgic.defect_tracker.enums.LoginType;
import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
public class Employee extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empId;

    private String firstName;

    private String lastName;

    private String gender;

    private String whatsappNumber;

    private String email;

    private LocalDate joinDate;


    @JsonIgnore
    private String password;

    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private LoginType loginType = LoginType.USER;

    @Column(name = "custom_permissions")
    private Boolean customPermissions = false;

    @ManyToOne
    @JoinColumn(name = "designationId")
    private Designation designation;




    //Employee <--> BenchAllocation
    @OneToMany(mappedBy = "employee")
    @JsonIgnore
    private List<BenchAllocation> benchAllocations;

    @OneToMany(mappedBy = "assignTo")
    @JsonIgnore
    private List<Defect> defectList;

    @ManyToOne
    private Role role;
    // Forgot password attributes
    private String forgetPasswordToken;

    private LocalDateTime forgetPasswordTokenExpiry;

}
