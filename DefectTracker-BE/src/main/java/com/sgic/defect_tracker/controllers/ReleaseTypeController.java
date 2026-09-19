package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.ReleaseTypeRequestDTO;
import com.sgic.defect_tracker.dtos.response.ReleaseTypeResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.ReleaseTypeService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


@RestController
@RequestMapping(EndpointBundle.RELEASE_TYPE)
@CrossOrigin(origins = "*")

public class ReleaseTypeController {

    private final ReleaseTypeService releaseTypeService;

    public ReleaseTypeController(ReleaseTypeService releaseTypeService) {
        this.releaseTypeService = releaseTypeService;
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<ReleaseTypeResponseDTO>> createReleaseType(
            @Valid @RequestBody ReleaseTypeRequestDTO requestDTO) {
        ReleaseTypeResponseDTO result = releaseTypeService.createReleaseType(requestDTO);
        ResponseWrapper<ReleaseTypeResponseDTO> response = new ResponseWrapper<>(
                RestApiResponseStatusCodes.CREATED,
                result,
                "Release type created successfully"
        );

        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @PutMapping(EndpointBundle.ID)
    public ResponseEntity<ResponseWrapper<ReleaseTypeResponseDTO>> updateReleaseType(
            @PathVariable Long id,
            @Valid @RequestBody ReleaseTypeRequestDTO requestDTO) {
        ReleaseTypeResponseDTO result = releaseTypeService.updateReleaseType(id, requestDTO);
       ResponseWrapper<ReleaseTypeResponseDTO> response = new ResponseWrapper<>(
               RestApiResponseStatusCodes.SUCCESS,
               result,
               "Release type updated successfully"
       );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(EndpointBundle.ID)
    public ResponseEntity<ResponseWrapper<Void>> deleteReleaseType(@PathVariable Long id) {
        releaseTypeService.deleteReleaseType(id);
        return ResponseEntity.ok(
                new ResponseWrapper<>(RestApiResponseStatusCodes.SUCCESS, null, "Release type deleted successfully")
        );
    }
    @GetMapping
    public ResponseEntity<ResponseWrapper<Page<ReleaseTypeResponseDTO>>> getAllReleaseTypes(Pageable pageable) {
        Page<ReleaseTypeResponseDTO> releaseTypes = releaseTypeService.getAllReleaseTypes(pageable);
        return ResponseEntity.ok(
                new ResponseWrapper<>(RestApiResponseStatusCodes.SUCCESS, releaseTypes, "Release types fetched successfully")
        );
    }
    @GetMapping(EndpointBundle.ID)
    public ResponseEntity<ResponseWrapper<ReleaseTypeResponseDTO>> getReleaseTypeById(@PathVariable Long id) {
        ReleaseTypeResponseDTO releaseType = releaseTypeService.getReleaseTypeById(id);
        return ResponseEntity.ok(
                new ResponseWrapper<>(RestApiResponseStatusCodes.SUCCESS, releaseType, "Release type fetched successfully")
        );
    }
}