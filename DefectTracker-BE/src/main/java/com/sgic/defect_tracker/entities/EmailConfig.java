package com.sgic.defect_tracker.entities;

import com.sgic.defect_tracker.utils.DateAudit;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class EmailConfig extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "from_email", nullable = false, unique = true)
    private String fromEmail;

    @Column(name = "host")
    private String host;

    @Column(name = "port")
    private Integer port;

    @Column(name = "status", nullable = false)
    private Boolean status = false;

    @Column(name = "smtp_username")
    private String smtpUserName;

    @Column(name = "smtp_password")
    private String smtpPassword;

    @Column(name = "from_name")
    private String fromName;
}
