package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.entities.EmailTemplate;
import com.sgic.defect_tracker.repositories.EmailTemplateRepository;
import com.sgic.defect_tracker.service.EmailTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;

    public EmailTemplateServiceImpl(
            EmailTemplateRepository emailTemplateRepository) {
        this.emailTemplateRepository = emailTemplateRepository;
    }

    @Override
    public EmailTemplate createEmailTemplate(EmailTemplate emailTemplate) {

        return emailTemplateRepository.save(emailTemplate);
    }

    @Override
    public List<EmailTemplate> getAllEmailTemplates() {

        return emailTemplateRepository.findAll();
    }

    @Override
    public EmailTemplate getEmailTemplateById(Long id) {

        return emailTemplateRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Email template not found with id: " + id
                        ));
    }

    @Override
    public EmailTemplate updateEmailTemplate(
            Long id,
            EmailTemplate emailTemplate) {

        EmailTemplate existingTemplate =
                emailTemplateRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Email template not found with id: " + id
                                ));

        if (emailTemplate.getEmailNotificationType() != null) {
            existingTemplate.setEmailNotificationType(
                    emailTemplate.getEmailNotificationType()
            );
        }

        if (emailTemplate.getSubject() != null) {
            existingTemplate.setSubject(
                    emailTemplate.getSubject()
            );
        }

        if (emailTemplate.getBody() != null) {
            existingTemplate.setBody(
                    emailTemplate.getBody()
            );
        }

        if (emailTemplate.getStatus() != null) {
            existingTemplate.setStatus(
                    emailTemplate.getStatus()
            );
        }

        return emailTemplateRepository.save(existingTemplate);
    }

    @Override
    public void deleteEmailTemplate(Long id) {

        EmailTemplate existingTemplate =
                emailTemplateRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Email template not found with id: " + id
                                ));

        emailTemplateRepository.delete(existingTemplate);
    }

    @Override
    public EmailTemplate resetTemplate(Long templateId) {

        EmailTemplate template =
                emailTemplateRepository.findById(templateId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Email template not found with id: "
                                                + templateId
                                )
                        );

        template.setSubject(template.getDefault_subject());
        template.setBody(template.getDefault_body());

        return emailTemplateRepository.save(template);
    }
}