package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseRequestDTO;
import com.sgic.defect_tracker.dtos.response.ReleaseTestCaseResponseDTO;
import com.sgic.defect_tracker.entities.ReleaseTestCase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReleaseTestCaseMapper {

    ReleaseTestCase toEntity(ReleaseTestCaseRequestDTO dto);

    @Mapping(
            source = "testCase.testCaseId",
            target = "testCaseId"
    )
    @Mapping(
            source = "testCase.testCaseName",
            target = "testCaseName"
    )
    @Mapping(
            source = "testCase.description",
            target = "description"
    )
    @Mapping(
            source = "testCase.testSteps",
            target = "testSteps"
    )

    @Mapping(
            source = "testCase.module.moduleId",
            target = "moduleId"
    )
    @Mapping(
            source = "testCase.module.moduleName",
            target = "moduleName"
    )

    @Mapping(
            source = "testCase.subModule.subModuleId",
            target = "subModuleId"
    )
    @Mapping(
            source = "testCase.subModule.subModuleName",
            target = "subModuleName"
    )

    @Mapping(
            source = "testCase.severity.severityId",
            target = "severityId"
    )
    @Mapping(
            source = "testCase.severity.severityName",
            target = "severityName"
    )

    @Mapping(
            source = "testCase.defectType.defectTypeId",
            target = "defectTypeId"
    )
    @Mapping(
            source = "testCase.defectType.defectTypeName",
            target = "defectTypeName"
    )

    @Mapping(
            source = "benchAllocation.benchAllocationId",
            target = "benchAllocationId"
    )

    @Mapping(
            source = "benchAllocation.employee.empId",
            target = "employeeId"
    )

    @Mapping(
            expression = "java(entity.getBenchAllocation() != null && entity.getBenchAllocation().getEmployee() != null ? entity.getBenchAllocation().getEmployee().getFirstName() + \" \" + entity.getBenchAllocation().getEmployee().getLastName() : null)",
            target = "employeeName"
    )

    @Mapping(
            source = "releaseTestCaseId",
            target = "releaseTestCaseId"
    )
    @Mapping(
            source = "passOrFail",
            target = "passOrFail"
    )

    @Mapping(
            source = "status",
            target = "status"
    )

    // Ignore defect fields here because they are created separately
    @Mapping(target = "defectId", ignore = true)
    @Mapping(target = "assignToId", ignore = true)
    @Mapping(target = "assignToName", ignore = true)
    @Mapping(target = "priorityId", ignore = true)
    @Mapping(target = "priorityName", ignore = true)

    ReleaseTestCaseResponseDTO toResponse(ReleaseTestCase entity);
}