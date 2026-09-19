package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.response.HistoryResponseDto;
import com.sgic.defect_tracker.entities.DefectHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HistoryMapper {

    @Mapping(source = "assignedBy.firstName", target = "assignedByName")
    @Mapping(source = "assignedTo.firstName", target = "assignedToName")
    @Mapping(source = "updatedBy.firstName", target = "updatedBy")
    HistoryResponseDto toDefectHistoryDto(
            DefectHistory history
    );

    List<HistoryResponseDto> toDefectHistoryDtoList(
            List<DefectHistory> histories
    );
}