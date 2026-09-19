package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.entities.EmailTemplate;

import java.util.List;

public interface EmailTemplateService {

    EmailTemplate createEmailTemplate(EmailTemplate emailTemplate);

    List<EmailTemplate> getAllEmailTemplates();

    EmailTemplate getEmailTemplateById(Long id);

    EmailTemplate updateEmailTemplate(Long id, EmailTemplate emailTemplate);

    void deleteEmailTemplate(Long id);

    EmailTemplate resetTemplate(Long templateId);
}