package com.sgic.defect_tracker.service;
import com.sgic.defect_tracker.dtos.request.DesignationRequestDTO;
import com.sgic.defect_tracker.dtos.response.DesignationResponseDTO;

import org.springframework.data.domain.Page;

public interface DesignationService {
   Page<DesignationResponseDTO>getAllDesignation(int page,int size);

   DesignationResponseDTO updateDesignation(Long designationId,DesignationRequestDTO requestDTO);

    DesignationResponseDTO createDesignation(DesignationRequestDTO requestDTO);

    void deleteDesignation(Long designationId);

    DesignationResponseDTO getByDesignationId(Long designationId);
}
