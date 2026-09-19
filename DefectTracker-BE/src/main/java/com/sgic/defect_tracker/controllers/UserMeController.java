package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.response.CurrentUserPermissionsDTO;
import com.sgic.defect_tracker.dtos.response.ProjectInfoDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.CurrentUserPermissionService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/me")
@RequiredArgsConstructor
public class UserMeController {

    private final CurrentUserPermissionService
            currentUserPermissionService;


    @GetMapping("/permissions")
    public ResponseEntity<ResponseWrapper<CurrentUserPermissionsDTO>>
    getMyPermissions(Authentication authentication) {

        String email = authentication.getName();

        CurrentUserPermissionsDTO response =
                currentUserPermissionService
                        .getMyPermissions(email);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        response
                )
        );
    }


    @GetMapping("/projects/{projectId}/permissions")
    public ResponseEntity<ResponseWrapper<CurrentUserPermissionsDTO>>
    getMyProjectPermissions(
            @PathVariable Long projectId,
            Authentication authentication
    ) {

        String email = authentication.getName();

        CurrentUserPermissionsDTO response =
                currentUserPermissionService
                        .getMyProjectPermissions(
                                email,
                                projectId
                        );

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        response
                )
        );
    }


    @GetMapping("/projects")
    public ResponseEntity<ResponseWrapper<List<ProjectInfoDTO>>> getMyProjects(
            Authentication authentication
    ) {
        String email = authentication.getName();

        List<ProjectInfoDTO> projects =
                currentUserPermissionService.getMyProjects(email);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        projects
                )
        );
    }
}