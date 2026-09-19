package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface EmailConfigRepository extends JpaRepository<EmailConfig, Long> {
    Optional<EmailConfig> findFirstByStatusTrue();
    List<EmailConfig> findByStatusTrue();

    Optional<EmailConfig> findByHostAndPortAndSmtpUserName(
            String host,
            Integer port,
            String smtpUserName
    );

    Optional<EmailConfig> findByFromEmail(String fromEmail);


    boolean existsByFromEmailAndIdNot(String fromEmail, Long id);
}
