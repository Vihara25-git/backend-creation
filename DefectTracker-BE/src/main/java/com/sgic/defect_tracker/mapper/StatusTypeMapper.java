package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.StatusTypeRequestDTO;
import com.sgic.defect_tracker.dtos.response.StatusTypeResponseDTO;
import com.sgic.defect_tracker.entities.StatusType;
import com.sgic.defect_tracker.repositories.StatusTypeRepository;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StatusTypeMapper {


    StatusType toEntity(StatusTypeRequestDTO dto);

    StatusTypeResponseDTO toResponse(StatusType entity);



}
