package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.RoleRequestDTO;
import com.sgic.defect_tracker.dtos.response.RoleResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.jmx.export.annotation.ManagedOperation;
import com.sgic.defect_tracker.entities.Role;

import java.util.List;


@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoleMapper {

    //requestdto to entity
    Role toEntity(RoleRequestDTO requestDTO);

    //entity to responsedto
    RoleResponseDTO toDto(Role role);

    //update

    void updateRoleFromDto(RoleRequestDTO requestDTO, @MappingTarget Role role);
    List<RoleResponseDTO> toDtoList(List<Role> roles);



}