package com.sgic.defect_tracker.entities;


import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;


@Entity
@Table(
        name = "privilege_template",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_privilege_type_subtype",
                        columnNames = {"type", "sub_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeTemplate extends DateAudit {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 60)
    private String type;

    @Column(name = "sub_type", nullable = false, length = 60)
    private String subType;

    @Column(name = "description", length = 255)
    private String description;
}