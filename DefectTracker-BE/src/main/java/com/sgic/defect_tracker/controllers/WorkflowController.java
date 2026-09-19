package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.WorkFlowRequestDTO;
import com.sgic.defect_tracker.dtos.response.StatusTypeResponseDTO;
import com.sgic.defect_tracker.dtos.response.WorkFlowResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.WorkFlowService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(EndpointBundle.WORKFLOW)
public class WorkflowController {
    private final WorkFlowService workFlowService;

    public WorkflowController(WorkFlowService workFlowService){
        this.workFlowService=workFlowService;}

    @PostMapping//(EndpointBundle.CREATEWORKFLOW)
    public ResponseEntity<ResponseWrapper<List<WorkFlowResponseDTO>>> createWorkFlow(
            @RequestBody List<WorkFlowRequestDTO> requestDTO) {

        List<WorkFlowResponseDTO> response =
                workFlowService.createWorkFlow(requestDTO);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        response
                )
        );
    }

    @DeleteMapping(EndpointBundle.DELETEALLWORKFLOW)
    public ResponseEntity<ResponseWrapper<Void>> deleteAllWorkFlows() {

        workFlowService.deleteAllWorkFlows();

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "All workflows deleted successfully",
                        null
                )
        );
    }

    @GetMapping("/sequence")
    public ResponseEntity<ResponseWrapper<List<StatusTypeResponseDTO>>> getWorkflowStatusSequence() {

        List<StatusTypeResponseDTO> sequence =
                workFlowService.getWorkflowStatusSequence();

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        sequence
                )
        );
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ResponseWrapper<List<WorkFlowResponseDTO>>> getWorkFlowsByStatusTypeId1(
            @PathVariable Long id) {

        List<WorkFlowResponseDTO> response =
                workFlowService.getWorkFlowsByStatusTypeId1(id);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        response
                )
        );
    }

    // GET ALL
    @GetMapping//(EndpointBundle.GETALLWORKFLOW)
    public ResponseEntity<ResponseWrapper<List<WorkFlowResponseDTO>>> getAllWorkFlows(){

        List<WorkFlowResponseDTO> response = workFlowService.getAllWorkFlows();

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        response
                )
        );
    }

    @GetMapping(EndpointBundle.GETFIRSTNODE)
    public ResponseEntity<ResponseWrapper<Long>> getStartingStatusTypeId() {

        Long startingStatusTypeId =
                workFlowService.getStartingStatusTypeId();

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        startingStatusTypeId
                )
        );
    }
}