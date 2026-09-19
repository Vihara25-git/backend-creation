package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.ReleaseViewRequestDto;
import com.sgic.defect_tracker.dtos.response.ReleaseViewResponseDto;
import com.sgic.defect_tracker.entities.ReleaseView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReleaseViewMapper {


    ReleaseView toEntity(ReleaseViewRequestDto releaseViewRequestDto);

    // Entity -> Response DTO
    // Entity -> Response DTO
    @Mapping(
            source = "releaseType.releaseTypeId",
            target = "releaseTypeId"
    )
    @Mapping(
            source = "projectDetails.projectId",
            target = "projectId"
    )
    // Entity -> Response DTO
    ReleaseViewResponseDto toDto(ReleaseView releaseView);


    //update
    void  updateReleaseViewFromDto(ReleaseViewRequestDto requestDto,@MappingTarget ReleaseView releaseView);
    List<ReleaseViewResponseDto>toDtoList(List<ReleaseView>releaseViews);



}
