package com.sgic.defect_tracker.service.impl;


import com.sgic.defect_tracker.dtos.request.EmailConfigRequestDTO;
import com.sgic.defect_tracker.dtos.response.EmailConfigResponseDTO;
import com.sgic.defect_tracker.entities.EmailConfig;
import com.sgic.defect_tracker.repositories.EmailConfigRepository;
import com.sgic.defect_tracker.service.EmailConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Optional;
import java.util.Properties;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailConfigServiceImpl implements EmailConfigService {

    private final EmailConfigRepository emailConfigRepository;

    public EmailConfig createEmailConfig(EmailConfigRequestDTO request) {

        // Validate email format
        validateEmail(request.getSmtpUserName(), "Username");
        validateEmail(request.getFromEmail(), "From Email");

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }

        EmailConfig emailConfig = new EmailConfig();
        // Check duplicate SMTP configuration
        Optional<EmailConfig> existingConfig =
                emailConfigRepository.findByHostAndPortAndSmtpUserName(
                        request.getHost(),
                        request.getPort(),
                        request.getSmtpUserName()
                );

        if (existingConfig.isPresent()) {
            throw new RuntimeException(
                    "Email configuration already exists for this SMTP server"
            );
        }

        Optional<EmailConfig> existingFromEmail =
                emailConfigRepository.findByFromEmail(request.getFromEmail());

        if (existingFromEmail.isPresent()) {
            throw new RuntimeException(
                    "From email already exists"
            );
        }


        if (request.getSmtpPassword() == null || request.getSmtpPassword().isBlank()) {
            throw new RuntimeException("SMTP password is required");
        }

        emailConfig.setName(request.getName());
        emailConfig.setFromEmail(request.getFromEmail());
        emailConfig.setFromName(request.getFromName());
        emailConfig.setHost(request.getHost());
        emailConfig.setPort(request.getPort());

        if (Boolean.TRUE.equals(request.getStatus())) {
            List<EmailConfig> activeConfigs = emailConfigRepository.findByStatusTrue();
            for (EmailConfig active : activeConfigs) {
                active.setStatus(false);
                emailConfigRepository.save(active);
            }
            emailConfig.setStatus(true);
        } else {
            emailConfig.setStatus(false);
        }

        emailConfig.setSmtpUserName(request.getSmtpUserName());
        emailConfig.setSmtpPassword(request.getSmtpPassword());

        return emailConfigRepository.save(emailConfig);
    }


    @Override
    public EmailConfigResponseDTO getEmailConfig(Long id) {

        EmailConfig emailConfig = emailConfigRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Email configuration not found with id: " + id)
                );

        return mapToResponseDTO(emailConfig);
    }


    @Override
    public EmailConfigResponseDTO updateEmailConfig(
            Long id,
            EmailConfigRequestDTO requestDTO
    ) {

        EmailConfig emailConfig = emailConfigRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Email configuration not found with id: " + id)
                );

        // Validate email format
        validateEmail(requestDTO.getSmtpUserName(), "Username");
        validateEmail(requestDTO.getFromEmail(), "From Email");

        if (requestDTO.getName() == null || requestDTO.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }

        // Check duplicate From Email
        boolean fromEmailExists =
                emailConfigRepository.existsByFromEmailAndIdNot(
                        requestDTO.getFromEmail(),
                        id
                );

        if (fromEmailExists) {
            throw new RuntimeException(
                    "From email already exists"
            );
        }

        // Determine password to use: use new password if provided, otherwise preserve existing password
        String passwordToUse = requestDTO.getSmtpPassword();
        boolean isNewPasswordProvided = (passwordToUse != null && !passwordToUse.trim().isEmpty());

        if (!isNewPasswordProvided) {
            passwordToUse = emailConfig.getSmtpPassword();
        }

        if (passwordToUse == null || passwordToUse.trim().isEmpty()) {
            throw new RuntimeException("SMTP password is required");
        }

        // Validate SMTP credentials only when a new password or host/port changed
        if (isNewPasswordProvided) {
            validateSmtpPassword(
                    requestDTO.getHost(),
                    requestDTO.getPort(),
                    requestDTO.getSmtpUserName(),
                    passwordToUse
            );
        }

        emailConfig.setName(requestDTO.getName());
        emailConfig.setFromEmail(requestDTO.getFromEmail());
        emailConfig.setFromName(requestDTO.getFromName());
        emailConfig.setHost(requestDTO.getHost());
        emailConfig.setPort(requestDTO.getPort());
        emailConfig.setSmtpUserName(requestDTO.getSmtpUserName());
        emailConfig.setSmtpPassword(passwordToUse);


        if (requestDTO.getStatus() != null) {
            if (Boolean.TRUE.equals(requestDTO.getStatus())) {
                List<EmailConfig> activeConfigs = emailConfigRepository.findByStatusTrue();
                for (EmailConfig active : activeConfigs) {
                    if (!active.getId().equals(id)) {
                        active.setStatus(false);
                        emailConfigRepository.save(active);
                    }
                }
                emailConfig.setStatus(true);
            } else {
                emailConfig.setStatus(false);
            }
        }


        EmailConfig updatedConfig = emailConfigRepository.save(emailConfig);
        return mapToResponseDTO(updatedConfig);

    }

    private void validateSmtpPassword(
            String host,
            Integer port,
            String username,
            String password
    ) {

        try {

            JavaMailSenderImpl mailSender =
                    new JavaMailSenderImpl();

            mailSender.setHost(host);
            mailSender.setPort(port);
            mailSender.setUsername(username);
            mailSender.setPassword(password);

            Properties props =
                    mailSender.getJavaMailProperties();

            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.writetimeout", "5000");

            var transport =
                    mailSender.getSession()
                            .getTransport("smtp");

            try {

                transport.connect(
                        host,
                        port,
                        username,
                        password
                );

            } finally {

                if (transport.isConnected()) {
                    transport.close();
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "SMTP validation failed: " + e.getMessage()
            );
        }
    }

    private void validateEmail(String email, String fieldName) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException(fieldName + " is required");
        }

        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (!email.matches(emailRegex)) {
            throw new RuntimeException(
                    fieldName + " must be a valid email address (e.g., example@domain.com)"
            );
        }
    }



    @Override
    public EmailConfigResponseDTO toggleStatus(Long id) {

        EmailConfig emailConfig = emailConfigRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Email configuration not found with id: " + id));

        // Currently enabled → disable it
        if (Boolean.TRUE.equals(emailConfig.getStatus())) {

            emailConfig.setStatus(false);

        } else {

            // Currently disabled → enable it
            // Disable all other active configurations
            List<EmailConfig> activeConfigs =
                    emailConfigRepository.findByStatusTrue();

            for (EmailConfig active : activeConfigs) {

                if (!active.getId().equals(id)) {
                    active.setStatus(false);
                    emailConfigRepository.save(active);
                }
            }

            emailConfig.setStatus(true);
        }

        EmailConfig updatedConfig =
                emailConfigRepository.save(emailConfig);

        return mapToResponseDTO(updatedConfig);
    }


    private EmailConfigResponseDTO mapToResponseDTO(EmailConfig emailConfig) {
        EmailConfigResponseDTO responseDTO = new EmailConfigResponseDTO();
        responseDTO.setId(emailConfig.getId());
        responseDTO.setName(emailConfig.getName());
        responseDTO.setFromEmail(emailConfig.getFromEmail());
        responseDTO.setFromName(emailConfig.getFromName());
        responseDTO.setHost(emailConfig.getHost());
        responseDTO.setPort(emailConfig.getPort());
        responseDTO.setStatus(emailConfig.getStatus());
        responseDTO.setSmtpUserName(emailConfig.getSmtpUserName());
//        responseDTO.setSmtpPassword(emailConfig.getSmtpPassword());
        return responseDTO;
    }



    @Override
    public List<EmailConfigResponseDTO> getAllEmailConfigs() {

        return emailConfigRepository.findAll()
                .stream()
                .sorted((a, b) -> {
                    boolean aStatus = Boolean.TRUE.equals(a.getStatus());
                    boolean bStatus = Boolean.TRUE.equals(b.getStatus());
                    if (aStatus != bStatus) {
                        return aStatus ? -1 : 1;
                    }
                    return Long.compare(a.getId() != null ? a.getId() : 0L, b.getId() != null ? b.getId() : 0L);
                })
                .map(this::mapToResponseDTO)
                .toList();
    }

//    @Override
//    public void sendTestEmail(
//            Long id,
//            String toEmail,
//            String subject,
//            String message) {

//        EmailConfig emailConfig = emailConfigRepository.findById(id)
//                .orElseThrow(() ->
//                        new RuntimeException(
//                                "Email configuration not found with id: " + id
//                        )
//                );

//        if (!Boolean.TRUE.equals(emailConfig.getStatus())) {
//            throw new RuntimeException(
//                    "Email configuration is disabled"
//            );
//        }
//
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//
//        mailSender.setHost(emailConfig.getHost());
//        mailSender.setPort(emailConfig.getPort());
//
//        mailSender.setUsername(emailConfig.getSmtpUserName());
//        mailSender.setPassword(emailConfig.getSmtpPassword());

//        Properties props = mailSender.getJavaMailProperties();
//
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//
//        SimpleMailMessage mailMessage = new SimpleMailMessage();
//
//        mailMessage.setFrom(emailConfig.getFromEmail());
//        mailMessage.setTo(toEmail);
//        mailMessage.setSubject(subject);
//        mailMessage.setText(message);
//
//        mailSender.send(mailMessage);
   // }

    @Override
    public void deleteEmailConfig(Long id) {

        EmailConfig emailConfig = emailConfigRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Email configuration not found with id: " + id)
                );

        if (Boolean.TRUE.equals(emailConfig.getStatus())) {
            throw new RuntimeException(
                    "Enabled email configuration cannot be deleted. Please disable it first."
            );
        }

        emailConfigRepository.delete(emailConfig);
    }

}