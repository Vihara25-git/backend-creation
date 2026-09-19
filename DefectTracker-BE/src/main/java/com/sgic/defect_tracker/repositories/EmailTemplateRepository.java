package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByEmailNotificationType(
            String emailNotificationType
    );
}