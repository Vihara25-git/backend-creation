package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.SubmoduleRequestDTO;
import com.sgic.defect_tracker.dtos.response.SubmoduleResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.SubmoduleService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(EndpointBundle.SUBMODULE_MODULE)
@RequiredArgsConstructor
public class SubmoduleController {

    private final SubmoduleService submoduleService;

    @PostMapping(EndpointBundle.CREATESUBMODULE)
    public ResponseEntity<ResponseWrapper<SubmoduleResponseDTO>> createSubmodule(
            @RequestBody SubmoduleRequestDTO requestDTO) {

        SubmoduleResponseDTO response =
                submoduleService.createSubModule(requestDTO);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        response
                )
        );
    }

    // GET ALL SUBMODULES FOR MODULE
    @GetMapping(EndpointBundle.SUBMODULE_BY_MODULE)
    public ResponseEntity<ResponseWrapper<List<SubmoduleResponseDTO>>> getAllSubmodulesByModule(
            @PathVariable Long moduleId) {

        List<SubmoduleResponseDTO> response =
                submoduleService.getAllSubmodulesByModule(moduleId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        response
                )
        );
    }

    // GET ONE SUBMODULE
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<SubmoduleResponseDTO>> getSubmoduleById(
            @PathVariable Long moduleId,
            @PathVariable Long id) {

        SubmoduleResponseDTO response =
                submoduleService.getbyID(id);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        response
                )
        );
    }

    // UPDATE
    @PutMapping(EndpointBundle.SUBMODULE_BY_ID)
    public ResponseEntity<ResponseWrapper<SubmoduleResponseDTO>> updateSubmodule(
            @PathVariable Long moduleId,
            @PathVariable Long submoduleId,
            @RequestBody SubmoduleRequestDTO requestDTO) {

        requestDTO.setModuleId(moduleId);

        SubmoduleResponseDTO response =
                submoduleService.updateSubmodule(submoduleId, requestDTO);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.UPDATE_SUCCESSFULL,
                        response
                )
        );
    }

    // DELETE
    @DeleteMapping(EndpointBundle.SUBMODULE_BY_ID)
    public ResponseEntity<ResponseWrapper<Void>> deleteSubmodule(
            @PathVariable Long moduleId,
            @PathVariable Long submoduleId) {

        submoduleService.deleteSubmodule(submoduleId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "SubModule deleted successfully",
                        null
                )
        );
    }
}