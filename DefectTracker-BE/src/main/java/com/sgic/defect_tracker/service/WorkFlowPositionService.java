package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.WorkFlowPositionRequestDTO;
import com.sgic.defect_tracker.dtos.response.WorkFlowPositionResponseDTO;

import java.util.List;

public interface WorkFlowPositionService {

    List<WorkFlowPositionResponseDTO> savePositions(
            List<WorkFlowPositionRequestDTO> requestDTOs
    );

    List<WorkFlowPositionResponseDTO> getAllPositions();

    void deleteAllPositions();
}