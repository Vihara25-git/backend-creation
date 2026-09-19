package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.WorkFlowRequestDTO;
import com.sgic.defect_tracker.dtos.response.StatusTypeResponseDTO;
import com.sgic.defect_tracker.dtos.response.WorkFlowResponseDTO;

import java.util.List;

public interface WorkFlowService {

    List<WorkFlowResponseDTO> createWorkFlow(
            List<WorkFlowRequestDTO> requestDTOs);

    void deleteAllWorkFlows();

    List<WorkFlowResponseDTO> getWorkFlowsByStatusTypeId1(
            Long statusTypeId1);

    List<WorkFlowResponseDTO> getAllWorkFlows();

    Long getStartingStatusTypeId();

    List<StatusTypeResponseDTO> getWorkflowStatusSequence();
}