package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.WorkFlowPositionRequestDTO;
import com.sgic.defect_tracker.dtos.response.WorkFlowPositionResponseDTO;
import com.sgic.defect_tracker.entities.WorkFlowPosition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkFlowPositionMapper {

    @Mapping(source = "statusTypeId", target = "statusType.statusTypeId")
    WorkFlowPosition toEntity(WorkFlowPositionRequestDTO dto);

    @Mapping(source = "statusType.statusTypeId", target = "statusTypeId")
    WorkFlowPositionResponseDTO toDto(WorkFlowPosition entity);
}