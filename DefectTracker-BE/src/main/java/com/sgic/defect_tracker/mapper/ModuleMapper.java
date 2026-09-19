package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.ModuleRequestDto;
import com.sgic.defect_tracker.dtos.response.ModuleResponseDto;
import com.sgic.defect_tracker.entities.Module;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ModuleMapper {

      @Mapping(target = "project", ignore = true)
    @Mapping(target = "defects", ignore = true)
    @Mapping(target = "subModule", ignore = true)
  //  @Mapping(target = "employees", ignore = true)

    @Mapping(target = "modQAS", ignore = true)
    Module toEntity(ModuleRequestDto dto);

    @Mapping(source = "project.projectId", target = "projectId")
    @Mapping(source = "project.projectName", target = "projectName")
    @Mapping(source = "subModule", target = "subModules")
    ModuleResponseDto toDto(Module module);

    void updateModuleFromDto(
            ModuleRequestDto dto,
            @MappingTarget Module module
    );

   // List<ModuleResponseDto> toDtoList(List<Module> modules);
}
