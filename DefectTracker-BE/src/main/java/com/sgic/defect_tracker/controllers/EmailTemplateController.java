package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.entities.EmailTemplate;
import com.sgic.defect_tracker.service.EmailTemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//import static com.sgic.defect_tracker.utils.EndpointBundle.EMAIL_TEMPLATE;
import static com.sgic.defect_tracker.utils.EndpointBundle.EMAIL_TEMPLATE;
import static com.sgic.defect_tracker.utils.EndpointBundle.EMAIL_TEMPLATE_ID;

@RestController

@RequestMapping(EMAIL_TEMPLATE)
public class EmailTemplateController {


    private final EmailTemplateService emailTemplateService;

    public EmailTemplateController(
            EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<EmailTemplate> createEmailTemplate(
            @RequestBody EmailTemplate emailTemplate) {

        EmailTemplate createdTemplate =
                emailTemplateService.createEmailTemplate(emailTemplate);

        return new ResponseEntity<>(
                createdTemplate,
                HttpStatus.CREATED
        );
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<EmailTemplate>> getAllEmailTemplates() {

        List<EmailTemplate> templates =
                emailTemplateService.getAllEmailTemplates();

        return ResponseEntity.ok(templates);
    }

    // GET BY ID
    @GetMapping(EMAIL_TEMPLATE_ID)
    public ResponseEntity<EmailTemplate> getEmailTemplateById(
            @PathVariable Long id) {

        EmailTemplate template =
                emailTemplateService.getEmailTemplateById(id);

        return ResponseEntity.ok(template);
    }

    // UPDATE
    @PutMapping(EMAIL_TEMPLATE_ID)
    public ResponseEntity<EmailTemplate> updateEmailTemplate(
            @PathVariable Long id,
            @RequestBody EmailTemplate emailTemplate) {

        EmailTemplate updatedTemplate =
                emailTemplateService.updateEmailTemplate(
                        id,
                        emailTemplate
                );

        return ResponseEntity.ok(updatedTemplate);
    }

    // DELETE
    @DeleteMapping(EMAIL_TEMPLATE_ID)
    public ResponseEntity<Void> deleteEmailTemplate(
            @PathVariable Long id) {

        emailTemplateService.deleteEmailTemplate(id);

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{templateId}/reset")
    public ResponseEntity<EmailTemplate> resetTemplate(
            @PathVariable Long templateId
    ) {

        EmailTemplate resetTemplate =
                emailTemplateService.resetTemplate(templateId);

        return ResponseEntity.ok(resetTemplate);
    }
}