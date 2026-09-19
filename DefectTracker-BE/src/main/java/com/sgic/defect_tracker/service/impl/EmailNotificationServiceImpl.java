package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.entities.EmailConfig;
import com.sgic.defect_tracker.entities.EmailTemplate;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.RoleBasedPreference;
import com.sgic.defect_tracker.entities.UserBasedPerferences;
import com.sgic.defect_tracker.repositories.BenchAllocationRepository;
import com.sgic.defect_tracker.repositories.EmailConfigRepository;
import com.sgic.defect_tracker.repositories.EmailTemplateRepository;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.repositories.RoleBasedPreferenceRepository;
import com.sgic.defect_tracker.repositories.UserBasedPerferencesRepository;
import com.sgic.defect_tracker.service.EmailNotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl
        implements EmailNotificationService {

    private final EmailConfigRepository emailConfigRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final EmployeeRepository employeeRepository;
    private final UserBasedPerferencesRepository userPreferenceRepository;
    private final RoleBasedPreferenceRepository rolePreferenceRepository;
    private final BenchAllocationRepository benchAllocationRepository;

    private static final Set<String> SYSTEM_NOTIFICATION_TYPES = Set.of(
            "EMPLOYEE_CREATED",
            "PASSWORD_RESET"
    );

    @Async
    @Override
    @Transactional(readOnly = true)
    public void sendEmail(
            String notificationType,
            String toEmail,
            Map<String, Object> variables) {

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Recipient email is empty for notification type: {}", notificationType);
            return;
        }

        // 1. Get enabled email configuration
        Optional<EmailConfig> emailConfigOpt = emailConfigRepository.findFirstByStatusTrue();
        if (emailConfigOpt.isEmpty()) {
            log.warn("No enabled email configuration found. Skipping email for: {}", notificationType);
            return;
        }
        EmailConfig emailConfig = emailConfigOpt.get();

        // 2. Get email template
        Optional<EmailTemplate> templateOpt = emailTemplateRepository.findByEmailNotificationType(notificationType);
        if (templateOpt.isEmpty()) {
            log.warn("Email template not found for notification type: {}", notificationType);
            return;
        }
        EmailTemplate emailTemplate = templateOpt.get();

        // 3. Check whether email template is enabled
        if (!Boolean.TRUE.equals(emailTemplate.getStatus())) {
            log.info("Email template is disabled: {}", notificationType);
            return;
        }

        // 4. Check recipient notification preferences (bypass for system account emails)
        if (!SYSTEM_NOTIFICATION_TYPES.contains(notificationType)) {
            if (!isEmailEnabledForRecipient(toEmail, emailTemplate.getTemplateId())) {
                log.info("Email notification skipped for {} (type: {}) due to channel preference set to None/WhatsApp",
                        toEmail, notificationType);
                return;
            }
        }

        // 5. Replace variables in subject
        String subject = replaceVariables(
                emailTemplate.getSubject(),
                variables
        );

        // 6. Replace variables in HTML body
        String body = replaceVariables(
                emailTemplate.getBody(),
                variables
        );

        // 7. Configure SMTP
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

        // 8. Create MIME message for HTML email
        MimeMessage mimeMessage =
                mailSender.createMimeMessage();

        try {

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mimeMessage,
                            false,
                            "UTF-8"
                    );

            helper.setFrom(
                    emailConfig.getFromEmail()
            );

            helper.setTo(toEmail.trim());

            helper.setSubject(subject);

            // true = send body as HTML
            helper.setText(
                    body,
                    true
            );

            // 9. Send email
            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {} for notification: {}", toEmail, notificationType);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", toEmail, e);
            throw new RuntimeException(
                    "Failed to send email to: "
                            + toEmail,
                    e
            );
        }
    }

    /**
     * Checks if email notifications are enabled for the recipient based on:
     * 1. Role based preferences (direct role & active bench allocations) - Non-removable baseline
     * 2. User specific preferences (can ADD extra email notifications)
     */
    private boolean isEmailEnabledForRecipient(String email, Long templateId) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email.trim());
        if (employeeOpt.isEmpty()) {
            log.debug("Employee not found for email: {}, skipping email dispatch", email);
            return false;
        }

        Employee employee = employeeOpt.get();

        // 1. Check Role-based Preferences first (Baseline / Non-removable privileges)
        Set<Long> roleIds = new HashSet<>();
        if (employee.getRole() != null && employee.getRole().getRoleId() != null) {
            roleIds.add(employee.getRole().getRoleId());
        }

        // Include all project allocation roles associated with the employee
        try {
            List<BenchAllocation> allocations = benchAllocationRepository
                    .findAllAllocationsByEmployeeId(employee.getEmpId());
            if (allocations != null) {
                for (BenchAllocation alloc : allocations) {
                    if (alloc.getRole() != null && alloc.getRole().getRoleId() != null) {
                        roleIds.add(alloc.getRole().getRoleId());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch bench allocations for empId: {}", employee.getEmpId(), e);
        }

        for (Long roleId : roleIds) {
            Optional<RoleBasedPreference> rolePref = rolePreferenceRepository
                    .findByRole_RoleIdAndEmailTemplate_TemplateId(roleId, templateId);

            if (rolePref.isPresent() && Boolean.TRUE.equals(rolePref.get().getStatus())) {
                String channel = rolePref.get().getChannel();
                if (channel != null) {
                    String normalized = channel.trim().toLowerCase();
                    if ("email".equals(normalized) || "both".equals(normalized)) {
                        return true;
                    }
                }
            }
        }

        // 2. Check User-specific Extra Preferences (Can only ADD email notification)
        Optional<UserBasedPerferences> userPref = userPreferenceRepository
                .findByEmployee_EmpIdAndEmailTemplate_TemplateId(employee.getEmpId(), templateId);

        if (userPref.isPresent() && Boolean.TRUE.equals(userPref.get().getStatus())) {
            String channel = userPref.get().getChannel();
            if (channel != null) {
                String normalized = channel.trim().toLowerCase();
                if ("email".equals(normalized) || "both".equals(normalized)) {
                    return true;
                }
            }
        }

        // If no active preference allows email for this template/role, do not send
        return false;
    }

    /**
     * Replace template placeholders with actual values.
     *
     * Example:
     * {{employeeName}} -> Mithusha
     * {{email}}        -> user@example.com
     */
    private String replaceVariables(
            String content,
            Map<String, Object> variables) {

        if (content == null) {
            return "";
        }

        String result = content;

        if (variables == null || variables.isEmpty()) {
            return result;
        }

        for (Map.Entry<String, Object> entry :
                variables.entrySet()) {

            String placeholder =
                    "{{" + entry.getKey() + "}}";

            String value =
                    entry.getValue() != null
                            ? String.valueOf(
                            entry.getValue()
                    )
                            : "";

            result = result.replace(
                    placeholder,
                    value
            );
        }

        return result;
    }
}