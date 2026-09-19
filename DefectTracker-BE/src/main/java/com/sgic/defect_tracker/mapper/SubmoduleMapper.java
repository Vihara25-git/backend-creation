package com.sgic.defect_tracker.mapper;

import ch.qos.logback.core.model.ComponentModel;
import com.sgic.defect_tracker.dtos.request.SubmoduleRequestDTO;
import com.sgic.defect_tracker.dtos.response.SubmoduleResponseDTO;
import com.sgic.defect_tracker.entities.SubModule;
import com.sgic.defect_tracker.entities.SubmoduleDev;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface SubmoduleMapper {

    @Mapping(source = "module.moduleId", target = "moduleId")
    @Mapping(target = "developerIds",
    expression = "java(getDeveloperIds(subModule))"
    )
    SubmoduleResponseDTO toDto(SubModule subModule);


    @Mapping(target = "module", ignore = true)
    SubModule toEntity(SubmoduleRequestDTO dto);


    @Mapping(target = "subModuleId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(
            SubmoduleRequestDTO dto,
            @MappingTarget SubModule submodule
    );

    default List<Long> getDeveloperIds(SubModule subModule) {
        if (subModule.getSubmoduleDevs() == null) {
            return Collections.emptyList();
        }
        return subModule.getSubmoduleDevs()
                .stream()
                .map(SubmoduleDev::getEmployee)
                .filter(employee -> employee != null)
                .map(employee -> employee.getEmpId())
                .collect(Collectors.toList());
    }
}
