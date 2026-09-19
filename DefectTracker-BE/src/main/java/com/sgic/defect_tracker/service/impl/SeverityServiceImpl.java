package com.sgic.defect_tracker.service.impl;


import com.sgic.defect_tracker.dtos.request.SeverityRequestDTO;
import com.sgic.defect_tracker.dtos.response.SeverityResponseDTO;
import com.sgic.defect_tracker.entities.Severity;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.mapper.SeverityMapper;
import com.sgic.defect_tracker.repositories.SeverityRepository;
import com.sgic.defect_tracker.service.SeverityService;
import com.sgic.defect_tracker.utils.ValidationMessages;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SeverityServiceImpl implements SeverityService {

    private final SeverityRepository severityRepository;
    private final SeverityMapper severityMapper;

    @Autowired
    public SeverityServiceImpl(SeverityRepository severityRepository, SeverityMapper severityMapper) {
        this.severityRepository = severityRepository;
        this.severityMapper = severityMapper;
    }

    @Override
    public Page<SeverityResponseDTO> getAllSeverity(Pageable pageable) {
        return severityRepository.findAllByOrderByWeightAsc(pageable)
                .map(severityMapper::toDto);
    }

    @Override
    public SeverityResponseDTO CreateSeverity(SeverityRequestDTO severityRequestDTO) {

        if (severityRepository.existsByNormalizedSeverityName(
                severityRequestDTO.getSeverityName())) {

            throw new RuntimeException(
                    "Severity name already exists"
            );
        }

        if (severityRepository.existsByWeight(severityRequestDTO.getWeight())) {
            throw new IllegalArgumentException("A severity with this weight already exists.");
        }

        Severity severity = severityMapper.toEntity(severityRequestDTO);

        try {
            Severity savedSeverity = severityRepository.save(severity);
            return severityMapper.toDto(savedSeverity);
        } catch (DataIntegrityViolationException e) {
            // Two requests raced past the check above at the same time — the DB constraint caught it
            throw new IllegalArgumentException("A severity with this weight already exists.");
        }
    }

    @Override
    @Transactional
    public SeverityResponseDTO updateSeverityById(Long id, SeverityRequestDTO severityRequestDTO) {

        if (severityRepository.existsByNormalizedSeverityNameAndIdNot(
                severityRequestDTO.getSeverityName(),
                id)) {

            throw new RuntimeException(
                    "Severity name already exists"
            );
        }

        Severity severity = severityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Severity with ID " + id + " not found"));

        if (severityRepository.existsByWeightAndSeverityIdNot(severityRequestDTO.getWeight(), id)) {
            throw new IllegalArgumentException("A severity with this weight already exists.");
        }

        severityMapper.updateEntityFromDto(severityRequestDTO, severity);

        try {
            Severity updatedSeverity = severityRepository.save(severity);
            return severityMapper.toDto(updatedSeverity);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("A severity with this weight already exists.");
        }
    }

    public void deleteSeverity(Long id){
        Severity severity = severityRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(ValidationMessages.INVALID_ID));
        severityRepository.delete(severity);
    }

}





