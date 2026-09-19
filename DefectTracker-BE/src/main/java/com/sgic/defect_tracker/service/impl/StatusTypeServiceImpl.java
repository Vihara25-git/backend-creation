package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.StatusTypeRequestDTO;
import com.sgic.defect_tracker.dtos.response.StatusTypeResponseDTO;
import com.sgic.defect_tracker.entities.StatusType;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.mapper.StatusTypeMapper;
import com.sgic.defect_tracker.repositories.DefectRepository;
import com.sgic.defect_tracker.repositories.StatusTypeRepository;
import com.sgic.defect_tracker.repositories.WorkflowRepository;
import com.sgic.defect_tracker.service.StatusTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusTypeServiceImpl implements StatusTypeService {

    private final StatusTypeRepository repository;
    private final DefectRepository defectRepository;
    private final StatusTypeMapper mapper;
    private final WorkflowRepository workflowRepository;

    public StatusTypeServiceImpl(
            StatusTypeRepository repository,
            DefectRepository defectRepository,
            StatusTypeMapper mapper,
            WorkflowRepository workflowRepository
    ) {
        this.repository = repository;
        this.defectRepository = defectRepository;
        this.mapper = mapper;
        this.workflowRepository = workflowRepository;
    }



    @Override
    public StatusTypeResponseDTO save(StatusTypeRequestDTO dto) {

        validateStatusTypeForCreate(dto);

        StatusType entity = mapper.toEntity(dto);

        entity.setStatusName(dto.getStatusName().trim());
        entity.setStatusType(dto.getStatusType().trim());
        entity.setColorCode(dto.getColorCode().trim());

        StatusType saved = repository.save(entity);

        return mapper.toResponse(saved);
    }



    @Override
    public StatusTypeResponseDTO UpdateStatusType(
            long statusTypeId,
            StatusTypeRequestDTO statusTypeRequestDTO
    ) {

        StatusType statusType = repository.findById(statusTypeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Status Type not found"
                        )
                );

        String newStatusName =
                statusTypeRequestDTO.getStatusName() == null
                        ? ""
                        : statusTypeRequestDTO.getStatusName().trim();

        String newStatusType =
                statusTypeRequestDTO.getStatusType() == null
                        ? ""
                        : statusTypeRequestDTO.getStatusType().trim();

        String newColorCode =
                statusTypeRequestDTO.getColorCode() == null
                        ? ""
                        : statusTypeRequestDTO.getColorCode().trim();



        boolean sameStatusName =
                statusType.getStatusName() != null
                        && statusType.getStatusName()
                        .equalsIgnoreCase(newStatusName);

        boolean sameStatusType =
                statusType.getStatusType() != null
                        && statusType.getStatusType()
                        .equalsIgnoreCase(newStatusType);

        boolean sameColorCode =
                statusType.getColorCode() != null
                        && statusType.getColorCode()
                        .equalsIgnoreCase(newColorCode);

        if (sameStatusName
                && sameStatusType
                && sameColorCode) {

            throw new IllegalArgumentException(
                    "No changes detected. Please update any field before saving."
            );
        }



        if (repository.existsByStatusNameIgnoreCaseAndStatusTypeIdNot(
                newStatusName,
                statusTypeId
        )) {

            throw new IllegalArgumentException(
                    "Status name already exists."
            );
        }



        if (repository.existsByStatusTypeIgnoreCaseAndStatusTypeIdNot(
                newStatusType,
                statusTypeId
        )) {

            throw new IllegalArgumentException(
                    "Status Type already exists."
            );
        }


        if (repository.existsByColorCodeIgnoreCaseAndStatusTypeIdNot(
                newColorCode,
                statusTypeId
        )) {

            throw new IllegalArgumentException(
                    "Color code already exists."
            );
        }



        statusType.setStatusName(newStatusName);
        statusType.setStatusType(newStatusType);
        statusType.setColorCode(newColorCode);

        StatusType saved = repository.save(statusType);

        return mapper.toResponse(saved);
    }



    @Override
    public List<StatusTypeResponseDTO> GetStatusType() {

        List<StatusType> statusTypes = repository.findAll();

        return statusTypes.stream()
                .map(mapper::toResponse)
                .toList();
    }



    @Override
    public Page<StatusTypeResponseDTO> GetStatusType(
            Pageable pageable
    ) {

        return repository.findAll(pageable)
                .map(mapper::toResponse);
    }



    @Override
    public void delete(Long id) {

        StatusType statusType = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Status Type not found"
                        )
                );

        /*
        if (workflowRepository.existsByStatusType_StatusTypeId(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete. This status type is already used by one or more work flow."
            );
        }
        */

        if (defectRepository.existsByStatusType_StatusTypeId(id)) {

            throw new IllegalArgumentException(
                    "Cannot delete. This status type is already used by one or more defects."
            );
        }

        repository.delete(statusType);
    }



    private void validateStatusTypeForCreate(
            StatusTypeRequestDTO dto
    ) {

        if (dto.getStatusName() == null
                || dto.getStatusName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Status name is required."
            );
        }

        if (dto.getStatusType() == null
                || dto.getStatusType().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Status Type is required."
            );
        }

        if (dto.getColorCode() == null
                || dto.getColorCode().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Color code is required."
            );
        }

        String statusName =
                dto.getStatusName().trim();

        String statusType =
                dto.getStatusType().trim();

        String colorCode =
                dto.getColorCode().trim();

        // Check duplicate status name
        if (repository.existsByStatusNameIgnoreCase(
                statusName
        )) {

            throw new IllegalArgumentException(
                    "Status name already exists."
            );
        }

        // Check duplicate status type
        if (repository.existsByStatusTypeIgnoreCase(
                statusType
        )) {

            throw new IllegalArgumentException(
                    "Status Type already exists."
            );
        }

        // Check duplicate color
        if (repository.existsByColorCodeIgnoreCase(
                colorCode
        )) {

            throw new IllegalArgumentException(
                    "Color code already exists."
            );
        }
    }
}