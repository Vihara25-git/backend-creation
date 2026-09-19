package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Subselect;

@Entity
//@Subselect("""
//    SELECT
//        template_id,
//        body,
//        subject,
//        status,
//        email_notification_type
//    FROM email_template
//""")
@Table(name = "email_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "default_body", nullable = false,columnDefinition = "TEXT")
    private String default_body;

    @Column(name = "default_subject", nullable = false)
    private String default_subject;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "email_notification_type", nullable = false)
    private String emailNotificationType;
}