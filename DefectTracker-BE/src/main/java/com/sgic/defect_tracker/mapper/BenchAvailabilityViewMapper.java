package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.response.BenchAvailabilityViewResponseDTO;
import com.sgic.defect_tracker.entities.BenchAvailabilityView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BenchAvailabilityViewMapper {

//    @Mapping(source = "employee.whatsappNumber", target = "whatsappNumber")
//    @Mapping(source = "employee.email", target = "email")
    BenchAvailabilityViewResponseDTO toResponse(BenchAvailabilityView entity);

}
