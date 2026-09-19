package com.sgic.defect_tracker.entities;

import org.hibernate.annotations.Immutable;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Subselect;

import java.time.LocalDateTime;

@Entity
@Immutable
@Subselect("""
    SELECT
        employee_id,
        employee_name,
        designation_name,
        total_allocated_percentage,
        available_percentage,
        available_from,
        current_projects
    FROM bench_availability_view
""")
//         role_type,
//@Table(name = "bench_availability_view")
@Data
public class BenchAvailabilityView {

    //    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long benchId;
    @Id
    @Column(name = "employee_id")
    private Long empId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "designation_name")
    private String designationName;

    @Column(name = "total_allocated_percentage")
    private Long totalAllocatedPercentage;

    @Column(name = "available_percentage")
    private Long availablePercentage;

    @Column(name = "available_from")
    private LocalDateTime availablePeriod;

    @Column(name = "current_projects")
    private String currentProjects;
//
//    @Column(name = "role_type")
//    private String roleType;


}

//
//@Entity
//@Immutable
////@Subselect("""
////    SELECT
////        employee_id,
////        employee_name,
////        designation_name,
////        total_allocated_percentage,
////        available_percentage,
////        available_from,
////        current_projects
////    FROM bench_availability_view
////    """)
//@Table(name = "bench_availability_view")
//@Data
//public class BenchAvailabilityView {
//
//    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long benchId;
//
//    @Column(name = "employee_id")
//    private Long empId;
//
//    @Column(name = "employee_name")
//    private String employeeName;
//
//    @Column(name = "designation_name")
//    private String designationName;
//
//    @Column(name = "total_allocated_percentage")
//    private Long totalAllocatedPercentage;
//
//    @Column(name = "available_percentage")
//    private Long availablePercentage;
//
//    @Column(name = "available_from")
//    private LocalDateTime availableFrom;
//
//    @Column(name = "current_projects")
//    private String currentProjects;
//}

/**
 *  /**--CREATE OR REPLACE VIEW bench_availability_view AS
 * SELECT
 *     e.emp_id AS employee_id,
 *
 *     CONCAT(
 *         e.first_name,
 *         ' ',
 *         e.last_name
 *     ) AS employee_name,
 *
 *     d.designation_name,
 *
 *     r.role_type,
 *
 *     COALESCE(
 *         SUM(
 *             CASE
 *                 WHEN ba.start_date <= CURRENT_TIMESTAMP
 *                  AND ba.end_date >= CURRENT_TIMESTAMP
 *                 THEN ba.availability
 *                 ELSE 0
 *             END
 *         ),
 *         0
 *     ) AS total_allocated_percentage,
 *
 *     GREATEST(
 *         0,
 *         100 - COALESCE(
 *             SUM(
 *                 CASE
 *                     WHEN ba.start_date <= CURRENT_TIMESTAMP
 *                      AND ba.end_date >= CURRENT_TIMESTAMP
 *                     THEN ba.availability
 *                     ELSE 0
 *                 END
 *             ),
 *             0
 *         )
 *     ) AS available_percentage,
 *
 *     CASE
 *         WHEN COUNT(
 *             CASE
 *                 WHEN ba.start_date <= CURRENT_TIMESTAMP
 *                  AND ba.end_date >= CURRENT_TIMESTAMP
 *                 THEN 1
 *                 ELSE NULL
 *             END
 *         ) > 0
 *         THEN MAX(
 *             CASE
 *                 WHEN ba.start_date <= CURRENT_TIMESTAMP
 *                  AND ba.end_date >= CURRENT_TIMESTAMP
 *                 THEN ba.end_date
 *                 ELSE NULL
 *             END
 *         )
 *         ELSE NULL
 *     END AS available_from,
 *
 *     STRING_AGG(
 *         DISTINCT
 *         CASE
 *             WHEN ba.start_date <= CURRENT_TIMESTAMP
 *              AND ba.end_date >= CURRENT_TIMESTAMP
 *             THEN pd.project_name
 *             ELSE NULL
 *         END,
 *         ', '
 *     ) AS current_projects
 *
 * FROM employee e
 *
 * LEFT JOIN designation d
 *     ON d.designation_id = e.designation_id
 *
 * LEFT JOIN bench_allocation ba
 *     ON ba.emp_id = e.emp_id
 *
 * LEFT JOIN role r
 *     ON r.role_Id = ba.role_Id
 *
 * LEFT JOIN project_details pd
 *     ON pd.project_id = ba.project_id
 *
 * WHERE e.is_active = true
 * GROUP BY
 *     e.emp_id,
 *     e.first_name,
 *     e.last_name,
 *     d.designation_name,
 *     r.role_type;*/

