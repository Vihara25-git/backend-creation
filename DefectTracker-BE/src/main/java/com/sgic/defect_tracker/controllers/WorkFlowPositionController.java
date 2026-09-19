package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.WorkFlowPositionRequestDTO;
import com.sgic.defect_tracker.dtos.response.WorkFlowPositionResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.WorkFlowPositionService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/status/workflow-position")
public class WorkFlowPositionController {

    private final WorkFlowPositionService workFlowPositionService;

    public WorkFlowPositionController(
            WorkFlowPositionService workFlowPositionService) {

        this.workFlowPositionService = workFlowPositionService;
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<List<WorkFlowPositionResponseDTO>>>
    savePositions(
            @RequestBody List<WorkFlowPositionRequestDTO> requestDTOs) {

        List<WorkFlowPositionResponseDTO> response =
                workFlowPositionService.savePositions(requestDTOs);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        response
                )
        );
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<WorkFlowPositionResponseDTO>>>
    getAllPositions() {

        List<WorkFlowPositionResponseDTO> response =
                workFlowPositionService.getAllPositions();

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        response
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<ResponseWrapper<Void>> deleteAllPositions() {

        workFlowPositionService.deleteAllPositions();

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "All workflow positions deleted successfully",
                        null
                )
        );
    }
}