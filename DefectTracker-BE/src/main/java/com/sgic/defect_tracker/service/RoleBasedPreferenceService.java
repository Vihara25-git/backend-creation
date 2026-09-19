package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.RoleBasedPreferenceRequestDTO;
import com.sgic.defect_tracker.dtos.request.RoleNotificationUpdateRequestDTO;
import com.sgic.defect_tracker.dtos.response.RoleBasedPreferenceResponseDTO;

import java.util.List;
import java.util.Map;

public interface RoleBasedPreferenceService {

    RoleBasedPreferenceResponseDTO create(
            RoleBasedPreferenceRequestDTO request
    );

    List<RoleBasedPreferenceResponseDTO> getAll();

    RoleBasedPreferenceResponseDTO getById(Long id);

    RoleBasedPreferenceResponseDTO update(
            Long id,
            RoleBasedPreferenceRequestDTO request
    );

    void delete(Long id);

    List<RoleBasedPreferenceResponseDTO> getByRoleId(Long roleId);

    Map<Long, String> getRoleChannels(Long roleId);

    void updateRoleChannels(
            RoleNotificationUpdateRequestDTO request
    );


}