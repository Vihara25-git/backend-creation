//package com.sgic.defect_tracker.mapper;
//
//import com.sgic.defect_tracker.dtos.response.RoleBasedPreferenceResponseDTO;
//import com.sgic.defect_tracker.entities.RoleBasedPreference;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RoleBasedPerferencesMapper {
//
//    public RoleBasedPreferenceResponseDTO toResponse(
//            RoleBasedPreference entity) {
//
//        RoleBasedPreferenceResponseDTO response =
//                new RoleBasedPreferenceResponseDTO();
//
//        response.setReceiverId(entity.getReceiverId());
//
//        if (entity.getRole() != null) {
//            response.setRoleId(entity.getRole().getRoleId());
//            response.setRoleName(entity.getRole().getRoleName());
//        }
//
//        if (entity.getEmailTemplate() != null) {
//            response.setTemplateId(
//                    entity.getEmailTemplate().getTemplateId()
//            );
//
//            response.setSubject(
//                    entity.getEmailTemplate().getSubject()
//            );
//
//            response.setEmailNotificationType(
//                    entity.getEmailTemplate()
//                            .getEmailNotificationType()
//            );
//        }
//
//        response.setStatus(entity.getStatus());
//
//        return response;
//    }
//}