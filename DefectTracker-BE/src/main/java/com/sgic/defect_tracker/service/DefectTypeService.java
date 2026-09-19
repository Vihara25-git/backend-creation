package com.sgic.defect_tracker.service;


import com.sgic.defect_tracker.dtos.request.DefectTypeRequestDto;
import com.sgic.defect_tracker.dtos.response.DefectTypeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DefectTypeService {

    String defectdelete(Long id);
    Page<DefectTypeResponseDto> defectgetall(Pageable pageable);

    DefectTypeResponseDto defectsave(DefectTypeRequestDto defect);
    DefectTypeResponseDto updatedefect(DefectTypeRequestDto defect,Long defectTypeId);

}
