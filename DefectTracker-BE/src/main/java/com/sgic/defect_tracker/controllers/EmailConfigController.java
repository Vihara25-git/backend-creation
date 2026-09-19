
package com.sgic.defect_tracker.controllers;


import com.sgic.defect_tracker.dtos.request.EmailConfigRequestDTO;
import com.sgic.defect_tracker.dtos.response.EmailConfigResponseDTO;
import com.sgic.defect_tracker.entities.EmailConfig;

import com.sgic.defect_tracker.service.EmailConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email-config")
@RequiredArgsConstructor
public class EmailConfigController {

    private final EmailConfigService emailConfigService;



    @PostMapping("/create")
    public ResponseEntity<EmailConfig> createEmailConfig(
            @RequestBody EmailConfigRequestDTO request) {

        EmailConfig emailConfig =
                emailConfigService.createEmailConfig(request);

        return new ResponseEntity<>(emailConfig, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailConfigResponseDTO> getEmailConfig(
            @PathVariable Long id
    ) {

        EmailConfigResponseDTO response =
                emailConfigService.getEmailConfig(id);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    public ResponseEntity<EmailConfigResponseDTO> updateEmailConfig(
            @PathVariable Long id,
            @RequestBody EmailConfigRequestDTO requestDTO
    ) {

        EmailConfigResponseDTO response =
                emailConfigService.updateEmailConfig(id, requestDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EmailConfigResponseDTO>> getAllEmailConfigs() {

        List<EmailConfigResponseDTO> response =
                emailConfigService.getAllEmailConfigs();

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmailConfig(@PathVariable Long id) {

        emailConfigService.deleteEmailConfig(id);

        return ResponseEntity.ok("Email configuration deleted successfully");
    }

//    @PostMapping("/{id}/send")
//    public ResponseEntity<String> sendTestEmail(
//            @PathVariable Long id,
//            @RequestParam String toEmail,
//            @RequestParam String subject,
//            @RequestParam String message) {
//
//        emailConfigService.sendTestEmail(
//                id,
//                toEmail,
//                subject,
//                message
//        );
//
//        return ResponseEntity.ok(
//                "Email sent successfully"
//        );
//
//    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<EmailConfigResponseDTO> toggleEmailConfig(@PathVariable Long id) {
        EmailConfigResponseDTO response = emailConfigService.toggleStatus(id);
        return ResponseEntity.ok(response);
    }

}

//package com.sgic.defect_tracker.controllers;
//
//
//import com.sgic.defect_tracker.dtos.request.EmailConfigRequestDTO;
//import com.sgic.defect_tracker.dtos.response.EmailConfigResponseDTO;
//import com.sgic.defect_tracker.entities.EmailConfig;
//
//import com.sgic.defect_tracker.service.EmailConfigService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/email-config")
//@RequiredArgsConstructor
//public class EmailConfigController {
//
//    private final EmailConfigService emailConfigService;
//
//
//
//    @PostMapping("/create")
//    public ResponseEntity<EmailConfig> createEmailConfig(
//            @RequestBody EmailConfigRequestDTO request) {
//
//        EmailConfig emailConfig =
//                emailConfigService.createEmailConfig(request);
//
//        return new ResponseEntity<>(emailConfig, HttpStatus.CREATED);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<EmailConfigResponseDTO> getEmailConfig(
//            @PathVariable Long id
//    ) {
//
//        EmailConfigResponseDTO response =
//                emailConfigService.getEmailConfig(id);
//
//        return ResponseEntity.ok(response);
//    }
//
//
//    @PutMapping("/{id}")
//    public ResponseEntity<EmailConfigResponseDTO> updateEmailConfig(
//            @PathVariable Long id,
//            @RequestBody EmailConfigRequestDTO requestDTO
//    ) {
//
//        EmailConfigResponseDTO response =
//                emailConfigService.updateEmailConfig(id, requestDTO);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<EmailConfigResponseDTO>> getAllEmailConfigs() {
//
//        List<EmailConfigResponseDTO> response =
//                emailConfigService.getAllEmailConfigs();
//
//        return ResponseEntity.ok(response);
//    }
//
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteEmailConfig(@PathVariable Long id) {
//
//        emailConfigService.deleteEmailConfig(id);
//
//        return ResponseEntity.ok("Email configuration deleted successfully");
//    }
//
//    @PostMapping("/{id}/send")
//    public ResponseEntity<String> sendTestEmail(
//            @PathVariable Long id,
//            @RequestParam String toEmail,
//            @RequestParam String subject,
//            @RequestParam String message) {
//
//        emailConfigService.sendTestEmail(
//                id,
//                toEmail,
//                subject,
//                message
//        );
//
//        return ResponseEntity.ok(
//                "Email sent successfully"
//        );
//
//    }
//
//    @PutMapping("/{id}/toggle")
//    public ResponseEntity<EmailConfigResponseDTO> toggleEmailConfig(@PathVariable Long id) {
//        EmailConfigResponseDTO response = emailConfigService.toggleStatus(id);
//        return ResponseEntity.ok(response);
//    }
//
//}