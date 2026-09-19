package com.sgic.defect_tracker.mapper;


import com.sgic.defect_tracker.dtos.request.ClientDetailsRequestDto;
import com.sgic.defect_tracker.dtos.request.ProjectDetailsRequestDto;
import com.sgic.defect_tracker.dtos.response.ClientDetailsResponseDto;
import com.sgic.defect_tracker.dtos.response.ProjectDetailsResponseDto;
import com.sgic.defect_tracker.entities.ClientDetails;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.ProjectDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectDetailsMapper {

    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "clientDetails", source = "dto.clientDetails")
    ProjectDetails toEntity(ProjectDetailsRequestDto dto);


    @Mapping(source = "status", target = "status")
    @Mapping(source = "projectManager.empId", target = "projectManagerId")
    @Mapping(source = "projectManager.firstName", target = "projectManagerName")
    @Mapping(
            source = "projectManager.designation.designationId",
            target = "projectManagerDesignationId"
    )
    @Mapping(
            source = "projectManager.designation.designationName",
            target = "designationName"
    )
    ProjectDetailsResponseDto toResponseDto(ProjectDetails entity);

    @Mapping(target = "clientId", ignore = true)
    ClientDetails toClientEntity(ClientDetailsRequestDto dto);

    ClientDetailsResponseDto toClientResponseDto(ClientDetails entity);

    // update
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "projectManager", ignore = true)
    @Mapping(target = "clientDetails", source = "clientDetails")
    @Mapping(target = "status", source = "status")
    void updateEntityFromDto(
            ProjectDetailsRequestDto dto,
            @MappingTarget ProjectDetails entity
    );
}
