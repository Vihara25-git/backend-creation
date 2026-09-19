package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.response.CurrentUserPermissionsDTO;
import com.sgic.defect_tracker.dtos.response.ProjectInfoDTO;

import java.util.List;

public interface CurrentUserPermissionService {
    List<ProjectInfoDTO> getMyProjects(String email);

    CurrentUserPermissionsDTO getMyPermissions(String email);

    CurrentUserPermissionsDTO getMyProjectPermissions(
            String email,
            Long projectId
    );
}