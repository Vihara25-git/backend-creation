package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.RoleRequestDTO;
import com.sgic.defect_tracker.dtos.response.RoleResponseDTO;
import com.sgic.defect_tracker.entities.Role;
import com.sgic.defect_tracker.exceptions.DuplicateResourceException;
import com.sgic.defect_tracker.mapper.RoleMapper;
import com.sgic.defect_tracker.repositories.RoleRepository;
import com.sgic.defect_tracker.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    // CREATE ROLE
    @Override
    public RoleResponseDTO saveRole(RoleRequestDTO requestDTO) {

        try {

            // Check duplicate role name
            if (roleRepository.existsByRoleNameIgnoreCase(
                    requestDTO.getRoleName())) {

                throw new DuplicateResourceException(
                        "Role Name already exists: " + requestDTO.getRoleName()
                );
            }

            // Check duplicate role type
            if (roleRepository.existsByRoleType(
                    requestDTO.getRoleType())) {

                throw new DuplicateResourceException(
                        "Role type already exists: " + requestDTO.getRoleType()
                );
            }

            // DTO -> Entity
            Role role = roleMapper.toEntity(requestDTO);

            // Save
            Role savedRole = roleRepository.save(role);

            // Entity -> DTO
            return roleMapper.toDto(savedRole);

        } catch (DuplicateResourceException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create role.",
                    e
            );
        }
    }

    // UPDATE ROLE
    @Override
    public RoleResponseDTO updateRole(
            Long roleId,
            RoleRequestDTO requestDTO) {

        try {

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() ->
                            new RuntimeException("Role not found")
                    );

            // Duplicate role name excluding current role
            if (roleRepository.existsByRoleNameIgnoreCaseAndRoleIdNot(
                    requestDTO.getRoleName(),
                    roleId)) {

                throw new DuplicateResourceException(
                        "Role Name already exists: " + requestDTO.getRoleName()
                );
            }

            // Duplicate role type excluding current role
            if (roleRepository.existsByRoleTypeAndRoleIdNot(
                    requestDTO.getRoleType(),
                    roleId)) {

                throw new DuplicateResourceException(
                        "Role type already exists: " + requestDTO.getRoleType()
                );
            }

            roleMapper.updateRoleFromDto(
                    requestDTO,
                    role
            );

            Role updatedRole = roleRepository.save(role);

            return roleMapper.toDto(updatedRole);

        } catch (DuplicateResourceException  e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to update role.",
                    e
            );
        }
    }


    @Override
    public Page<RoleResponseDTO> getAllRole(int page, int size){

        Pageable pageable = PageRequest.of(page, size);
        Page<Role> roles = roleRepository.findAll(pageable);
        return roles.map(roleMapper::toDto);


    }

    @Override
    public RoleResponseDTO getByRoleId(Long roleId){
        Role role = roleRepository.findById(roleId)
                .orElseThrow(()-> new RuntimeException("Role not found"));

        return roleMapper.toDto(role);

    }

    @Override
    public void deleteRole(Long roleId){
        Role role = roleRepository.findById(roleId)
                .orElseThrow(()-> new RuntimeException("Role not found"));

        roleRepository.delete(role);
    }

}
