package com.sgic.defect_tracker.service;


import com.sgic.defect_tracker.dtos.request.SeverityRequestDTO;
import com.sgic.defect_tracker.dtos.response.SeverityResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SeverityService {
    SeverityResponseDTO CreateSeverity(SeverityRequestDTO severityRequestDTO);
    Page<SeverityResponseDTO> getAllSeverity(Pageable pageable);
    void deleteSeverity(Long id);
    SeverityResponseDTO updateSeverityById(Long id, SeverityRequestDTO severityRequestDTO);

}

