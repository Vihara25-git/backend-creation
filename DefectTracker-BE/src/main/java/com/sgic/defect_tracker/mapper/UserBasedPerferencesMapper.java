package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.response.UserBasedPerferencesResponseDTO;
import com.sgic.defect_tracker.entities.UserBasedPerferences;
import org.springframework.stereotype.Component;

@Component
public class UserBasedPerferencesMapper {

    public UserBasedPerferencesResponseDTO toResponse(
            UserBasedPerferences entity) {

        UserBasedPerferencesResponseDTO response =
                new UserBasedPerferencesResponseDTO();

        response.setReceiverId(
                entity.getReceiverId()
        );

        // Employee
        if (entity.getEmployee() != null) {

            response.setEmpId(
                    entity.getEmployee().getEmpId()
            );

            response.setEmployeeName(
                    entity.getEmployee().getFirstName()
                            + " "
                            + entity.getEmployee().getLastName()
            );
        }

        // Email Template
        if (entity.getEmailTemplate() != null) {

            response.setTemplateId(
                    entity.getEmailTemplate().getTemplateId()
            );

            response.setSubject(
                    entity.getEmailTemplate().getSubject()
            );

            response.setEmailNotificationType(
                    entity.getEmailTemplate()
                            .getEmailNotificationType()
            );
        }

        response.setStatus(
                entity.getStatus()
        );

        return response;
    }
}