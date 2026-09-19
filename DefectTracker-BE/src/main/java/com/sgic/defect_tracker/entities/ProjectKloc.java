package com.sgic.defect_tracker.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_kloc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectKloc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kloc_id")
    private Long klocId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private ProjectDetails projectDetails;

//    @Column(name = "kilo_of_code", nullable = false)
//    private Double kiloOfCode;

    @DecimalMin(value = "0.1", inclusive = true)
    @Column(name = "kilo_of_code", nullable = false)
    private Double kiloOfCode = 0.1;
}