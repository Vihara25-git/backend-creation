package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.PrivilegeTemplateRequestDTO;
import com.sgic.defect_tracker.dtos.response.PrivilegeTemplateResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.PrivilegeTemplateService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.sgic.defect_tracker.utils.EndpointBundle.PRIVILEGE_TEMPLATES;
import static com.sgic.defect_tracker.utils.EndpointBundle.PRIVILEGE_TEMPLATES_ID;

@RestController
@RequestMapping(PRIVILEGE_TEMPLATES)
@RequiredArgsConstructor
public class PrivilegeTemplateController {

    private final PrivilegeTemplateService privilegeTemplateService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<PrivilegeTemplateResponseDTO>>> getAll() {
        List<PrivilegeTemplateResponseDTO> response = privilegeTemplateService.getAll();
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Privilege templates retrieved successfully",
                        response
                )
        );
    }

    @GetMapping(PRIVILEGE_TEMPLATES_ID)
    public ResponseEntity<ResponseWrapper<PrivilegeTemplateResponseDTO>> getById(@PathVariable Long id) {
        PrivilegeTemplateResponseDTO response = privilegeTemplateService.getById(id);
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Privilege template retrieved successfully",
                        response
                )
        );
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<PrivilegeTemplateResponseDTO>> create(
            @Valid @RequestBody PrivilegeTemplateRequestDTO request) {
        PrivilegeTemplateResponseDTO response = privilegeTemplateService.create(request);
        return new ResponseEntity<>(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.CREATED.getCode(),
                        "Privilege template created successfully",
                        response
                ),
                HttpStatus.CREATED
        );
    }

    @PutMapping(PRIVILEGE_TEMPLATES_ID)
    public ResponseEntity<ResponseWrapper<PrivilegeTemplateResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody PrivilegeTemplateRequestDTO request) {
        PrivilegeTemplateResponseDTO response = privilegeTemplateService.update(id, request);
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Privilege template updated successfully",
                        response
                )
        );
    }

    @DeleteMapping(PRIVILEGE_TEMPLATES_ID)
    public ResponseEntity<ResponseWrapper<Void>> delete(@PathVariable Long id) {
        privilegeTemplateService.delete(id);
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Privilege template deleted successfully",
                        null
                )
        );
    }
}
