package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.PriorityRequestDto;
import com.sgic.defect_tracker.dtos.response.PriorityResponseDto;
import com.sgic.defect_tracker.entities.Priority;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PriorityMapper {

//    @Mapping(target = "priorityId", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
    Priority toEntity(PriorityRequestDto dto);


    PriorityResponseDto toDto(Priority priority);


//    @Mapping(target = "priorityId", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(
            PriorityRequestDto dto,
            @MappingTarget Priority priority
    );
}