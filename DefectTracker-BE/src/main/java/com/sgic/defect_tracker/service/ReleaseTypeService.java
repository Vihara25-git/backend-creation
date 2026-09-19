package com.sgic.defect_tracker.service;


import com.sgic.defect_tracker.dtos.request.ReleaseTypeRequestDTO;
import com.sgic.defect_tracker.dtos.response.ReleaseTypeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReleaseTypeService {

    ReleaseTypeResponseDTO createReleaseType(ReleaseTypeRequestDTO requestDTO);
    ReleaseTypeResponseDTO updateReleaseType(Long id, ReleaseTypeRequestDTO requestDTO);

    Page<ReleaseTypeResponseDTO> getAllReleaseTypes(Pageable pageable);

    ReleaseTypeResponseDTO getReleaseTypeById(Long id);

    void deleteReleaseType(Long id);

}
