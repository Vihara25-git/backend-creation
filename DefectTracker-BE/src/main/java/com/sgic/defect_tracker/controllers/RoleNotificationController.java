package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.RoleNotificationUpdateRequestDTO;
import com.sgic.defect_tracker.service.RoleBasedPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.sgic.defect_tracker.utils.EndpointBundle.ROLE_ASSIGNED_POINTS;
import static com.sgic.defect_tracker.utils.EndpointBundle.ROLE_NOTIFICATIONS_UPDATE;

@RestController
@RequiredArgsConstructor
public class RoleNotificationController {

    private final RoleBasedPreferenceService rolePreferenceService;

    @GetMapping(ROLE_ASSIGNED_POINTS)
    public ResponseEntity<Map<Long, String>> getRoleChannels(
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(
                rolePreferenceService.getRoleChannels(roleId)
        );
    }

    @PutMapping(ROLE_NOTIFICATIONS_UPDATE)
    public ResponseEntity<Void> updateRoleChannels(
            @RequestBody RoleNotificationUpdateRequestDTO request
    ) {
        rolePreferenceService.updateRoleChannels(request);
        return ResponseEntity.noContent().build();
    }
}