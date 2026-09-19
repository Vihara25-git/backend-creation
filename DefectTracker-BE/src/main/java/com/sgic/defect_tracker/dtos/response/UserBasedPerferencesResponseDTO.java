package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

@Data
public class UserBasedPerferencesResponseDTO {

    private Long receiverId;

    private Long empId;

    private String employeeName;

    private Long templateId;

    private String emailNotificationType;

    private Boolean status;

    public void setSubject(String subject) {
    }
}