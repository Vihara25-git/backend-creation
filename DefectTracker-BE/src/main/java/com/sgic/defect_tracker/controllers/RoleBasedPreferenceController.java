
package com.sgic.defect_tracker.controllers;


import com.sgic.defect_tracker.dtos.request.RoleBasedPreferenceRequestDTO;
import com.sgic.defect_tracker.dtos.request.RoleNotificationUpdateRequestDTO;
import com.sgic.defect_tracker.dtos.response.RoleBasedPreferenceResponseDTO;
import com.sgic.defect_tracker.service.RoleBasedPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.sgic.defect_tracker.utils.EndpointBundle.*;

@RestController
@RequestMapping(ROLE_BASED_PREFERENCES)
@RequiredArgsConstructor
public class RoleBasedPreferenceController {

    private final RoleBasedPreferenceService rolePreferenceService;
    private final RoleBasedPreferenceService service;

    @PostMapping
    public ResponseEntity<RoleBasedPreferenceResponseDTO> create(
            @RequestBody RoleBasedPreferenceRequestDTO request) {

        return ResponseEntity.ok(
                rolePreferenceService.create(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<RoleBasedPreferenceResponseDTO>> getAll() {

        return ResponseEntity.ok(
                rolePreferenceService.getAll()
        );
    }

    @GetMapping(ROLE_BASED_PREFERENCES_ID)
    public ResponseEntity<RoleBasedPreferenceResponseDTO> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                rolePreferenceService.getById(id)
        );
    }
    @GetMapping("/role/{roleId}/assigned-points")
    public ResponseEntity<Map<Long, String>> getRoleChannels(
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(
                service.getRoleChannels(roleId)
        );
    }
//    @GetMapping(ROLE_ASSIGNED_POINTS)
//    public ResponseEntity<Map<Long, String>> getRoleChannels(
//            @PathVariable Long roleId
//    ) {
//        return ResponseEntity.ok(
//                rolePreferenceService.getRoleChannels(roleId)
//        );
//    }
    @PutMapping("/role-notifications/update")
    public ResponseEntity<Void> updateRoleChannels(
            @RequestBody RoleNotificationUpdateRequestDTO request
    ) {
        service.updateRoleChannels(request);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<RoleBasedPreferenceResponseDTO> update(
            @PathVariable Long id,
            @RequestBody RoleBasedPreferenceRequestDTO request) {

        return ResponseEntity.ok(
                rolePreferenceService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        rolePreferenceService.delete(id);

        return ResponseEntity.noContent().build();
    }
}


//package com.sgic.defect_tracker.controllers;
//
//import com.sgic.defect_tracker.dtos.request.RoleBasedPreferenceRequestDTO;
//import com.sgic.defect_tracker.dtos.response.RoleBasedPreferenceResponseDTO;
//import com.sgic.defect_tracker.service.RoleBasedPreferenceService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/role-preferences")
//@RequiredArgsConstructor
//public class RoleBasedPreferenceController {
//
//    private final RoleBasedPreferenceService roleBasedPreferenceService;
//
//    @PostMapping
//    public ResponseEntity<RoleBasedPreferenceResponseDTO> create(
//            @RequestBody RoleBasedPreferenceRequestDTO requestDTO) {
//
//        return ResponseEntity
//                .status(HttpStatus.CREATED)
//                .body(roleBasedPreferenceService.create(requestDTO));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<RoleBasedPreferenceResponseDTO>> getAll() {
//
//        return ResponseEntity.ok(
//                roleBasedPreferenceService.getAll()
//        );
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<RoleBasedPreferenceResponseDTO> getById(
//            @PathVariable Long id) {
//
//        return ResponseEntity.ok(
//                roleBasedPreferenceService.getById(id)
//        );
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<RoleBasedPreferenceResponseDTO> update(
//            @PathVariable Long id,
//            @RequestBody RoleBasedPreferenceRequestDTO requestDTO) {
//
//        return ResponseEntity.ok(
//                roleBasedPreferenceService.update(
//                        id,
//                        requestDTO
//                )
//        );
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(
//            @PathVariable Long id) {
//
//        roleBasedPreferenceService.delete(id);
//
//        return ResponseEntity.noContent().build();
//    }
//}