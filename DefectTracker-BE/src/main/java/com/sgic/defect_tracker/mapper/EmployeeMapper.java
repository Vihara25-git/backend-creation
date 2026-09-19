package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.EmployeeRequestDTO;
import com.sgic.defect_tracker.dtos.response.EmployeeResponseDTO;
import com.sgic.defect_tracker.entities.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMapper {

    Employee toEntity(EmployeeRequestDTO requestDTO);

    @Mapping(target = "password", ignore = true)
    @Mapping(source = "designation.designationId", target = "designationId")
    @Mapping(source = "designation.designationName", target = "designationName")
    EmployeeResponseDTO toDto(Employee employee);

    void updateEmployeeFromDto(EmployeeRequestDTO requestDTO, @MappingTarget Employee employee);

    //EmployeeResponseDTO toResponseDTO(Employee employee);
}


