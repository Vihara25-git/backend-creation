package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.DesignationRequestDTO;
import com.sgic.defect_tracker.dtos.response.DesignationResponseDTO;
import com.sgic.defect_tracker.entities.Designation;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DesignationMapper {

    //requestdto to entity
    Designation toEntity(DesignationRequestDTO dto);

    //entity to responsedto
    DesignationResponseDTO toDto(Designation designation);

    void updateDesignationFromDto(
            DesignationRequestDTO dto,
            @MappingTarget Designation designation);

    List<DesignationResponseDTO> toDtoList(List<Designation> designations );
}
