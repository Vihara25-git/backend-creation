package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.WorkFlowPositionRequestDTO;
import com.sgic.defect_tracker.dtos.response.WorkFlowPositionResponseDTO;
import com.sgic.defect_tracker.entities.StatusType;
import com.sgic.defect_tracker.entities.WorkFlowPosition;
import com.sgic.defect_tracker.mapper.WorkFlowPositionMapper;
import com.sgic.defect_tracker.repositories.WorkFlowPositionRepository;
import com.sgic.defect_tracker.service.WorkFlowPositionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkFlowPositionServiceImpl
        implements WorkFlowPositionService {

    private final WorkFlowPositionRepository workFlowPositionRepository;
    private final WorkFlowPositionMapper workFlowPositionMapper;

    public WorkFlowPositionServiceImpl(
            WorkFlowPositionRepository workFlowPositionRepository,
            WorkFlowPositionMapper workFlowPositionMapper) {

        this.workFlowPositionRepository = workFlowPositionRepository;
        this.workFlowPositionMapper = workFlowPositionMapper;
    }

    @Override
    public List<WorkFlowPositionResponseDTO> savePositions(
            List<WorkFlowPositionRequestDTO> requestDTOs) {

        List<WorkFlowPosition> positions = requestDTOs.stream()
                .map(workFlowPositionMapper::toEntity)
                .collect(Collectors.toList());

        List<WorkFlowPosition> savedPositions =
                workFlowPositionRepository.saveAll(positions);

        return savedPositions.stream()
                .map(workFlowPositionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkFlowPositionResponseDTO> getAllPositions() {

        return workFlowPositionRepository.findAll()
                .stream()
                .map(workFlowPositionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllPositions() {
        workFlowPositionRepository.deleteAll();
    }
}