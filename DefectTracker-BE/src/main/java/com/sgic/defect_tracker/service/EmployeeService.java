package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.ChangePasswordRequestDTO;
import com.sgic.defect_tracker.dtos.request.EmployeeRequestDTO;
import com.sgic.defect_tracker.dtos.response.EmployeeResponseDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
//    List<EmployeeResponseDTO> viewEmployees();

    EmployeeResponseDTO getEmployeeById(Long empId);
    Page<EmployeeResponseDTO> viewEmployees(Pageable pageable);


    EmployeeResponseDTO UpdateEmployee(Long empId, EmployeeRequestDTO employeeRequestDTO);

    EmployeeResponseDTO createEmployee(@Valid EmployeeRequestDTO employeeRequestDTO);

    EmployeeResponseDTO updateEmployee(Long empId,Boolean isActive);

    void DeleteEmployee(Long empId);

    List<EmployeeResponseDTO> filterEmployee(String keyword, String gender, String status, String designation);

    List<EmployeeResponseDTO> getEmployeesByDesignation(Long designationId);

    List<EmployeeResponseDTO> getEmployeesByEmployee(Long empId);

    void changePassword(ChangePasswordRequestDTO requestDTO);
}

