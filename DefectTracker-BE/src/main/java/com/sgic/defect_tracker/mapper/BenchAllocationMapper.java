package com.sgic.defect_tracker.mapper;


import com.sgic.defect_tracker.dtos.request.BenchAllocationRequestDTO;
import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;

import com.sgic.defect_tracker.entities.BenchAllocation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;



@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BenchAllocationMapper {

    @Mapping(target = "startDate", expression = "java(dto.getStartDate().atStartOfDay())")
    @Mapping(target = "endDate", expression = "java(dto.getEndDate().atTime(java.time.LocalTime.MAX))")

    BenchAllocation toEntity(BenchAllocationRequestDTO dto);


    @Mapping(source = "benchAllocationId", target = "benchId")
    @Mapping(source = "benchAllocationId", target = "benchAllocationId")
    @Mapping(source = "employee.empId", target = "empId")
    @Mapping(source = "employee.firstName", target = "firstName")
    @Mapping(source = "employee.lastName", target = "lastName")
    @Mapping(source = "employee.designation.designationId", target = "designationId")
    @Mapping(source = "employee.designation.designationName", target = "designationName")
    // NEW
    @Mapping(source = "employeeName", target = "employeeName")
    @Mapping(source = "employeeEmail", target = "employeeEmail")

    @Mapping(source = "projectDetails.projectId", target = "projectId")
    @Mapping(source = "projectDetails.projectName", target = "projectName")
    @Mapping(source = "role.roleId", target = "roleId")
    @Mapping(source = "role.roleType", target = "roleType")
    @Mapping(source = "role.roleName", target = "roleName")
    BenchAllocationResponseDTO toResponse(BenchAllocation entity);

//    void updateEntity(
//            BenchAllocationRequestDTO requestDTO,
//            @MappingTarget BenchAllocation entity
//    );
//@Mapping(
//        target = "startDate",
//        expression = "java(requestDTO.getStartDate().atStartOfDay())")
//@Mapping(
//        target = "endDate",
//        expression = "java(requestDTO.getEndDate().atTime(java.time.LocalTime.MAX))")
//void updateEntity(
//            BenchAllocationRequestDTO requestDTO,
//            @MappingTarget BenchAllocation entity
//    );

    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "projectDetails", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(
            target = "endDate",
            expression = "java(requestDTO.getEndDate().atTime(java.time.LocalTime.MAX))"
    )
    void updateEntity(
            BenchAllocationRequestDTO requestDTO,
            @MappingTarget BenchAllocation entity
    );

}
