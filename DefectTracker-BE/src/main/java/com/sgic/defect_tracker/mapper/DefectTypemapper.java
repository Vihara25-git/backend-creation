package com.sgic.defect_tracker.mapper;


import com.sgic.defect_tracker.dtos.request.DefectTypeRequestDto;
import com.sgic.defect_tracker.dtos.response.DefectTypeResponseDto;
import com.sgic.defect_tracker.entities.DefectType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DefectTypemapper {
    DefectType toDefectEntity(DefectTypeRequestDto defectDto);
    DefectTypeResponseDto toDefectDto(DefectType defectType);


    List<DefectTypeResponseDto> toDefectDtolist(List<DefectType> defects);


}
