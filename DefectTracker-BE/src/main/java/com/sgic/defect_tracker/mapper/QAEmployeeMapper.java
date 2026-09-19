package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.response.QAEmployeeResponseDTO;
import com.sgic.defect_tracker.entities.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy =
                NullValuePropertyMappingStrategy.IGNORE
)
public interface QAEmployeeMapper {

    QAEmployeeResponseDTO toResponse(Employee employee);
}