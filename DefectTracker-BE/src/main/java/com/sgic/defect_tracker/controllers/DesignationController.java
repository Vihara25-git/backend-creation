package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.DesignationRequestDTO;
import com.sgic.defect_tracker.dtos.response.DesignationResponseDTO;
import com.sgic.defect_tracker.dtos.response.EmployeeResponseDTO;
import com.sgic.defect_tracker.service.DesignationService;
import com.sgic.defect_tracker.service.EmployeeService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import jakarta.validation.Valid;

import java.util.List;

import static com.sgic.defect_tracker.utils.EndpointBundle.*;
import org.springframework.data.domain.Page;

@RestController
@RequiredArgsConstructor

@RequestMapping(EndpointBundle.Designation)

public class DesignationController {
    @Autowired
    private  DesignationService designationService;
    private final EmployeeService employeeService;

    @PostMapping(createdesignation)
    public ResponseEntity<ResponseWrapper<DesignationResponseDTO>> createDesignation(
             @Valid @RequestBody DesignationRequestDTO requestDTO) {

        DesignationResponseDTO response =
                designationService.createDesignation(requestDTO);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Designation created successfully.",
                        response
                )
        );
    }

    @GetMapping(get)
    public ResponseEntity<ResponseWrapper<Page<DesignationResponseDTO>>> getAllDesignation(
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "5")int size) {

        Page<DesignationResponseDTO> response = designationService.getAllDesignation(page,size);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        response
                )
        );
    }

    @GetMapping(getById)
    public ResponseEntity<ResponseWrapper<DesignationResponseDTO>> getByDesignationId(
            @PathVariable("designationId") Long designationId) {

        DesignationResponseDTO response = designationService.getByDesignationId(designationId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        RestApiResponseStatusCodes.SUCCESS.getMessage(),
                        response
                )
        );
    }

    @PutMapping(updatedesignationId)
    public ResponseEntity<ResponseWrapper<DesignationResponseDTO>> updateDesignation(
            @PathVariable("designationId") Long designationId,
            @Valid @RequestBody DesignationRequestDTO requestDTO) {

        DesignationResponseDTO response =
                designationService.updateDesignation(designationId, requestDTO);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Designation Updated successfully.",
                        response
                )
        );
    }

    @DeleteMapping(deletedesignationId)
    public ResponseEntity<ResponseWrapper<String>> deleteDesignation(
            @PathVariable Long designationId){

        designationService.deleteDesignation(designationId);



        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.DELETED.getCode(),
                        "Designation Deleted successfully.",
                        null
                )
        );
    }
    @GetMapping(EndpointBundle.Designation_Employee)
    public ResponseEntity<ResponseWrapper<List<EmployeeResponseDTO>>> getEmployeesByDesignation(
            @PathVariable Long designationId) {

        List<EmployeeResponseDTO> responseDTO =
                employeeService.getEmployeesByDesignation(designationId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTO
                )
        );
    }
}
