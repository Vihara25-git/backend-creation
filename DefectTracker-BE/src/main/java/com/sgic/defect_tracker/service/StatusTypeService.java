package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.StatusTypeRequestDTO;
import com.sgic.defect_tracker.dtos.response.StatusTypeResponseDTO;
import com.sgic.defect_tracker.entities.StatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StatusTypeService {
    StatusTypeResponseDTO UpdateStatusType(long statusTypeId, StatusTypeRequestDTO statusType);

    List<StatusTypeResponseDTO> GetStatusType();
    StatusTypeResponseDTO save(StatusTypeRequestDTO dto);

    void delete(Long id);


    Page<StatusTypeResponseDTO> GetStatusType(Pageable pageable);
}
