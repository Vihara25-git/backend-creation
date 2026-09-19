package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.RoleRequestDTO;
import com.sgic.defect_tracker.dtos.response.RoleResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoleService {

    //List<RoleResponseDTO> getAllRole();
    Page<RoleResponseDTO> getAllRole(int page, int size);

    RoleResponseDTO getByRoleId(Long roleId);

    void deleteRole(Long roleId);

    //Post
    RoleResponseDTO saveRole(RoleRequestDTO requestDTO);

    //Get
    RoleResponseDTO updateRole(Long roleId, RoleRequestDTO requestDTO);


}
