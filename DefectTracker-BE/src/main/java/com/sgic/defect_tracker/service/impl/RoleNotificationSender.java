package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.entities.EmailConfig;
import com.sgic.defect_tracker.entities.EmailTemplate;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.RoleBasedPreference;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.repositories.EmailConfigRepository;
import com.sgic.defect_tracker.repositories.EmailTemplateRepository;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.repositories.RoleBasedPreferenceRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class RoleNotificationSender {

    private final RoleBasedPreferenceRepository preferenceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailConfigRepository emailConfigRepository;

    /**
     * Sends an HTML email to all employees whose role has the selected
     * template configured with the EMAIL or BOTH channel.
     */
    @Transactional(readOnly = true)
    public void sendRoleNotification(
            Long templateId,
            Map<String, Object> variables
    ) {

        // 1. Get email template
        EmailTemplate template =
                emailTemplateRepository.findById(templateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Email template not found"
                                )
                        );

        // 2. Check template status
        if (!Boolean.TRUE.equals(template.getStatus())) {
            return;
        }

        // 3. Get enabled email configuration
        EmailConfig emailConfig =
                emailConfigRepository.findFirstByStatusTrue()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No enabled email configuration found"
                                )
                        );

        // 4. Replace variables in subject
        String subject = replaceVariables(
                template.getSubject(),
                variables
        );

        // 5. Replace variables in HTML body
        String body = replaceVariables(
                template.getBody(),
                variables
        );

        // 6. Get role-based preferences
        List<RoleBasedPreference> preferences =
                preferenceRepository
                        .findByEmailTemplate_TemplateIdAndStatusTrue(templateId);

        // 7. Configure SMTP
        JavaMailSenderImpl mailSender =
                createMailSender(emailConfig);

        // 8. Send email to employees
        for (RoleBasedPreference preference : preferences) {

            String channel =
                    normalizeChannel(preference.getChannel());

            /*
             * This sender handles email only.
             * WHATSAPP is handled separately.
             */
            if (!"email".equals(channel)
                    && !"both".equals(channel)) {
                continue;
            }

            Long roleId =
                    preference.getRole().getRoleId();

            List<Employee> employees =
                    employeeRepository.findByRole_RoleId(roleId);

            for (Employee employee : employees) {

                if (employee.getEmail() == null
                        || employee.getEmail().isBlank()) {
                    continue;
                }

                sendHtmlEmail(
                        mailSender,
                        emailConfig,
                        employee.getEmail(),
                        subject,
                        body
                );
            }
        }
    }

    /**
     * Creates JavaMailSender using the SMTP configuration
     * stored in the database.
     */
    private JavaMailSenderImpl createMailSender(
            EmailConfig emailConfig
    ) {

        JavaMailSenderImpl mailSender =
                new JavaMailSenderImpl();

        mailSender.setHost(
                emailConfig.getHost()
        );

        mailSender.setPort(
                emailConfig.getPort()
        );

        mailSender.setUsername(
                emailConfig.getSmtpUserName()
        );

        mailSender.setPassword(
                emailConfig.getSmtpPassword()
        );

        Properties properties =
                mailSender.getJavaMailProperties();

        properties.put(
                "mail.transport.protocol",
                "smtp"
        );

        properties.put(
                "mail.smtp.auth",
                "true"
        );

        properties.put(
                "mail.smtp.starttls.enable",
                "true"
        );

        return mailSender;
    }

    /**
     * Sends an HTML email.
     */
    private void sendHtmlEmail(
            JavaMailSenderImpl mailSender,
            EmailConfig emailConfig,
            String recipient,
            String subject,
            String body
    ) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            false,
                            "UTF-8"
                    );

            helper.setFrom(
                    emailConfig.getFromEmail()
            );

            helper.setTo(recipient);

            helper.setSubject(subject);

            // IMPORTANT:
            // true = body is HTML
            helper.setText(
                    body,
                    true
            );

            mailSender.send(message);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to send email to: "
                            + recipient,
                    e
            );
        }
    }

    /**
     * Normalizes the notification channel.
     */
    private String normalizeChannel(
            String channel
    ) {

        if (channel == null
                || channel.isBlank()) {

            return "none";
        }

        return channel
                .trim()
                .toLowerCase();
    }

    /**
     * Replaces template variables.
     *
     * Supports:
     * {{employeeName}}
     * {{ employeeName }}
     * {employeeName}
     */
    private String replaceVariables(
            String text,
            Map<String, Object> variables
    ) {

        if (text == null || text.isBlank()) {
            return "";
        }

        if (variables == null
                || variables.isEmpty()) {

            return text;
        }

        String result = text;

        for (Map.Entry<String, Object> entry :
                variables.entrySet()) {

            String key = entry.getKey();

            String value =
                    entry.getValue() != null
                            ? String.valueOf(
                            entry.getValue()
                    )
                            : "";

            result = result.replace(
                    "{{" + key + "}}",
                    value
            );

            result = result.replace(
                    "{{ " + key + " }}",
                    value
            );

            result = result.replace(
                    "{" + key + "}",
                    value
            );
        }

        return result;
    }
}