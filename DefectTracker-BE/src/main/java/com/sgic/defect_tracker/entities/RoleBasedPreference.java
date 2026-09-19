package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(
        name = "role_based_preference",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"role_id", "template_id"}
                )
        }
)
public class RoleBasedPreference extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_based_id")
    private Long roleBasedId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private EmailTemplate emailTemplate;

    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @Column(name = "channel", nullable = false)
    private String channel = "none";
}