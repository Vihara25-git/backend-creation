package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.PrivilegeTemplate;
import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "employee_privilege",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_template",
                        columnNames = {"employee_id", "template_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePrivilege extends DateAudit {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private PrivilegeTemplate template;
}