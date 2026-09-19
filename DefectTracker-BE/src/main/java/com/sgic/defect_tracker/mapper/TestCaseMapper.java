package com.sgic.defect_tracker.mapper;


import com.sgic.defect_tracker.dtos.request.TestCaseRequestDTO;
import com.sgic.defect_tracker.dtos.response.TestCaseResponseDTO;
import com.sgic.defect_tracker.entities.TestCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TestCaseMapper {

    //Request DTO -> Entity
    @Mapping(target = "testCaseId", ignore = true)
    @Mapping(target = "projectDetails", ignore = true)
    @Mapping(target = "module", ignore = true)
    @Mapping(target = "subModule", ignore = true)
    @Mapping(target = "severity", ignore = true)
    @Mapping(target = "defectType", ignore = true)
    TestCase toEntity(TestCaseRequestDTO dto);

    // Entity -> Response DTO
    @Mapping(source = "projectDetails.projectId", target = "projectId")
    @Mapping(source = "projectDetails.projectName", target = "projectName")

    @Mapping(source = "module.moduleId", target = "moduleId")
    @Mapping(source = "module.moduleName", target = "moduleName")

    @Mapping(source = "subModule.subModuleId", target = "subModuleId")
    @Mapping(source = "subModule.subModuleName", target = "subModuleName")

    @Mapping(source = "severity.severityId", target = "severityId")
    @Mapping(source = "severity.severityName", target = "severityName")

    @Mapping(source = "defectType.defectTypeId", target = "defectTypeId")
    @Mapping(source = "defectType.defectTypeName", target = "defectTypeName")
    TestCaseResponseDTO toResponseDTO(TestCase testCase);



    //update
    void updateTestCaseFromDto(TestCaseRequestDTO requestDTO, @MappingTarget TestCase testCase);
    List<TestCaseResponseDTO> toDtoList(List<TestCase> testCases);
}
