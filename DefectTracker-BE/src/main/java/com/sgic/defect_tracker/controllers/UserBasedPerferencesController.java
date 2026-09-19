package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.UserBasedPerferencesRequestDTO;
import com.sgic.defect_tracker.dtos.request.UserNotificationUpdateRequestDTO;
import com.sgic.defect_tracker.dtos.response.UserBasedPerferencesResponseDTO;
import com.sgic.defect_tracker.service.UserBasedPerferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.sgic.defect_tracker.utils.EndpointBundle.USER_BASED_PREFERENCES;
import static com.sgic.defect_tracker.utils.EndpointBundle.USER_BASED_PREFERENCES_ID;

@RestController
@RequestMapping(USER_BASED_PREFERENCES)            //"/api/v1/user-based-preferences"
@RequiredArgsConstructor
public class UserBasedPerferencesController {

    private final UserBasedPerferencesService userBasedPerferencesService;


    // =========================
    // CREATE
    // =========================

    @PostMapping
    public ResponseEntity<UserBasedPerferencesResponseDTO> create(
            @RequestBody UserBasedPerferencesRequestDTO requestDTO) {

        UserBasedPerferencesResponseDTO response =
                userBasedPerferencesService.create(requestDTO);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

//    @GetMapping("/user/{userId}/notification-preferences")
//    public ResponseEntity<Map<Long, String>> getUserChannels(
//            @PathVariable Long userId
//    ) {
//        return ResponseEntity.ok(
//                userBasedPerferencesService.getUserChannels(userId)
//        );
//    }
//
//    @PutMapping("/user/notification-preferences/update")
//    public ResponseEntity<Void> updateUserChannels(
//            @RequestBody UserNotificationUpdateRequestDTO request
//    ) {
//        userBasedPerferencesService.updateUserChannels(request);
//        return ResponseEntity.noContent().build();
//    }


    // =========================
    // GET ALL
    // =========================

    @GetMapping
    public ResponseEntity<List<UserBasedPerferencesResponseDTO>> getAll() {

        return ResponseEntity.ok(
                userBasedPerferencesService.getAll()
        );
    }


    // =========================
    // GET BY ID
    // =========================

    @GetMapping(USER_BASED_PREFERENCES_ID)
    public ResponseEntity<UserBasedPerferencesResponseDTO> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userBasedPerferencesService.getById(id)
        );
    }


    // =========================
    // UPDATE
    // =========================

    @PutMapping(USER_BASED_PREFERENCES_ID)
    public ResponseEntity<UserBasedPerferencesResponseDTO> update(
            @PathVariable Long id,
            @RequestBody UserBasedPerferencesRequestDTO requestDTO) {

        return ResponseEntity.ok(
                userBasedPerferencesService.update(
                        id,
                        requestDTO
                )
        );
    }


    // =========================
    // DELETE
    // =========================

    @DeleteMapping(USER_BASED_PREFERENCES_ID)
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        userBasedPerferencesService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
