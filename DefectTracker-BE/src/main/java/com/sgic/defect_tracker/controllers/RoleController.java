package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.response.RoleResponseDTO;
import com.sgic.defect_tracker.dtos.request.RoleRequestDTO;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.service.RoleService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(EndpointBundle.Role)
@RequiredArgsConstructor
public class RoleController {

    @Autowired
    private  RoleService roleService;

    //Create Role
    @PostMapping(EndpointBundle.CREATE)
    public ResponseEntity<ResponseWrapper<RoleResponseDTO>> saveRole(
            @RequestBody RoleRequestDTO requestDTO) {

        RoleResponseDTO response = roleService.saveRole(requestDTO);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Role created successfully.",
                        response
                )
        );
    }


    //Update Role
    @PutMapping("/update/{roleId}")
    public ResponseEntity<ResponseWrapper<String>> updateRole(
            @PathVariable Long roleId,
            @RequestBody RoleRequestDTO requestDTO) {

        roleService.updateRole(roleId, requestDTO);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.UPDATED.getCode(),
                        "Role updated successfully.",
                        null
                )
        );
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<Page<RoleResponseDTO>>> getAllRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<RoleResponseDTO> response = roleService.getAllRole(page, size);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Roles retrieved successfully.",
                        response
                )
        );
    }

    @GetMapping("/view/{roleId}")
    public ResponseEntity<ResponseWrapper<RoleResponseDTO>> getByRoleId(
            @PathVariable Long roleId) {

        RoleResponseDTO response = roleService.getByRoleId(roleId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Role retrieved successfully.",
                        response
                )
        );
    }

    @DeleteMapping("/delete/{roleId}")
    public ResponseEntity<ResponseWrapper<String>> deleteRole(
            @PathVariable Long roleId) {

        roleService.deleteRole(roleId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Role deleted successfully.",
                        "Success"
                )
        );
    }
}





