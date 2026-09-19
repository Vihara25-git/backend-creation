package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.EmployeeRequestDTO;
import com.sgic.defect_tracker.dtos.response.EmployeeResponseDTO;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.EmployeeService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.sgic.defect_tracker.utils.EndpointBundle.*;

@RestController
@RequestMapping(EndpointBundle.Employee)
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;


    @GetMapping(EndpointBundle.Employee_BY_DESIGNATION)
    public ResponseEntity<ResponseWrapper<Object>> getEmployeesByDesignation(
            @PathVariable Long designationId) {

        List<EmployeeResponseDTO> responseDTO = employeeService.getEmployeesByDesignation(designationId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTO
                )
        );
    }

    @GetMapping(EndpointBundle.Employee_GET + "/paged")
    public ResponseEntity<ResponseWrapper<Object>> viewEmployeesPaged(Pageable pageable) {

        Page<EmployeeResponseDTO> responseDTO = employeeService.viewEmployees(pageable);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTO
                )
        );
    }

    @GetMapping(Employee_ID)
    public ResponseEntity<ResponseWrapper<Object>> getEmployeeById(
            @PathVariable Long id) {

        List<EmployeeResponseDTO> responseDTO = employeeService.getEmployeesByEmployee(id);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTO
                )
        );
    }
    @PutMapping(EndpointBundle.Employee_UPDATE)
    public ResponseEntity<ResponseWrapper<Object>> UpdateEmployee(
            @PathVariable("empId") Long empId,
            @RequestBody EmployeeRequestDTO employeeRequestDTO) {

        EmployeeResponseDTO responseDTO = employeeService.UpdateEmployee(empId, employeeRequestDTO);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.UPDATE_SUCCESSFULL,
                        responseDTO
                )
        );
    }

    @PostMapping(EndpointBundle.Employee_CREATE)
    public ResponseEntity<ResponseWrapper<EmployeeResponseDTO>> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO employeeRequestDTO) {

        EmployeeResponseDTO response = employeeService.createEmployee(employeeRequestDTO);

        ResponseWrapper<EmployeeResponseDTO> wrapper =
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.SAVED_SUCCESSFULL,
                        response
                );

        return ResponseEntity.ok(wrapper);
    }

    @DeleteMapping(EndpointBundle.Employee_DELETE)
    public ResponseEntity<ResponseWrapper<Object>> DeleteEmployee(@PathVariable("tempId") Long empId) {

        employeeService.DeleteEmployee(empId);
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.DELETE_SUCCESS,
                        null
                )
        );
    }


    @PatchMapping(EndpointBundle.Employee_STATUS)
    public ResponseEntity<ResponseWrapper<Object>> updateEmployeeStatus(
            @PathVariable("empId") Long empId,
            @RequestBody java.util.Map<String, Boolean> body ) {

        Boolean isActive = body.get("isActive");
        EmployeeResponseDTO responseDTO = employeeService.updateEmployee(empId, isActive);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Status Updated Successfull",
                        responseDTO
                )
        );
    }



    // 🔹 Filter Employees (Search by keyword, gender, status, designation)
    @GetMapping(EndpointBundle.Employee_SEARCH)
    public ResponseEntity<ResponseWrapper<Object>> filterEmployees(

            @RequestParam(required = false, defaultValue = "") String keyword,

            @RequestParam(required = false, defaultValue = "") String gender,

            @RequestParam(required = false, defaultValue = "") String status,

            @RequestParam(required = false, defaultValue = "") String designation

    ) { // Convert empty values to null
        if(keyword.isBlank()){
            keyword = null;
        }


        if(gender.isBlank()){
            gender = null;
        }


        if(status.isBlank()){
            status = null;
        }


        if(designation.isBlank()){
            designation = null;
        }

        List<EmployeeResponseDTO> response =
                employeeService.filterEmployee(
                        keyword,
                        gender,
                        status,
                        designation
                );



        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        response
                )
        );

    }



}