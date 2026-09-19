package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.UserNotificationUpdateRequestDTO;
import com.sgic.defect_tracker.service.UserBasedPerferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserBasedPerferencesService userBasedPerferencesService;

    @GetMapping("/user/{userId}/notification-preferences")
    public ResponseEntity<Map<Long, String>> getUserChannels(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                userBasedPerferencesService.getUserChannels(userId)
        );
    }

    @PutMapping("/user/notification-preferences/update")
    public ResponseEntity<Void> updateUserChannels(
            @RequestBody UserNotificationUpdateRequestDTO request
    ) {
        userBasedPerferencesService.updateUserChannels(request);
        return ResponseEntity.noContent().build();
    }
}