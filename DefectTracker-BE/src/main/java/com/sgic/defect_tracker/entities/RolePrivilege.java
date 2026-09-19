package com.sgic.defect_tracker.entities;


import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
    @Table(
            name = "role_privilege",
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_role_template",
                            columnNames = {"role_id", "template_id"}
                    )
            }
    )
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class RolePrivilege extends DateAudit {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "role_id", nullable = false)
        private Role role;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "template_id", nullable = false)
        private PrivilegeTemplate template;

}
