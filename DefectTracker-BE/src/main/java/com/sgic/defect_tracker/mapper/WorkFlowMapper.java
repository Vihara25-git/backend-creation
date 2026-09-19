package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.WorkFlowRequestDTO;
import com.sgic.defect_tracker.dtos.response.WorkFlowResponseDTO;
import com.sgic.defect_tracker.entities.WorkFlow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WorkFlowMapper {

    @Mapping(source = "fromStatusId", target = "statusTypeId1")
    @Mapping(source = "toStatusId", target = "statusTypeId2")
    WorkFlow toEntity(WorkFlowRequestDTO dto);

    WorkFlowResponseDTO toDto(WorkFlow workFlow);
}