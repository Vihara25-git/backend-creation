package com.sgic.defect_tracker.service;

import java.util.Map;

public interface EmailNotificationService {

//    void sendEmail(
//            Long emailConfigId,
//            String notificationType,
//            String toEmail,
//            Map<String, Object> variables
//    );

    void sendEmail(
            String notificationType,
            String toEmail,
            Map<String, Object> variables);

}