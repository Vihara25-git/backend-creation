package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.SeverityRequestDTO;
import com.sgic.defect_tracker.dtos.response.SeverityResponseDTO;
import com.sgic.defect_tracker.entities.Severity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE )
public interface SeverityMapper {
     Severity toEntity(SeverityRequestDTO severityRequestDTO);   // map RequestDTO → Entity
     SeverityResponseDTO toDto(Severity severity);

     void updateEntityFromDto(SeverityRequestDTO dto, @MappingTarget Severity entity);// map Entity → ResponseDTO
}
