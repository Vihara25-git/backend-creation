package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.BenchAllocationRequestDTO;
import com.sgic.defect_tracker.dtos.request.StatusTypeRequestDTO;
import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.dtos.response.StatusTypeResponseDTO;
import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.BenchAllocationService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.sgic.defect_tracker.utils.EndpointBundle.BENCH_ALLOCATION_UPDATE;

import static com.sgic.defect_tracker.utils.EndpointBundle.*;
import com.sgic.defect_tracker.dtos.response.UserRoleResponseDTO;
import com.sgic.defect_tracker.repositories.BenchAllocationRepository;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
@RestController
@RequestMapping(BENCH_ALLOCATION )
public class BenchAllocationController {

    @Autowired
    private BenchAllocationService benchAllocationService;

    @Autowired
    private BenchAllocationRepository benchAllocationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;


    @PostMapping
    public ResponseEntity<ResponseWrapper<BenchAllocationResponseDTO>> createBenchAllocation(@Valid @RequestBody BenchAllocationRequestDTO dto){
        BenchAllocationResponseDTO responseDTO = benchAllocationService.save(dto);


        ResponseWrapper<BenchAllocationResponseDTO> wrapper =
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.SAVED_SUCCESSFULL,
                        responseDTO
                );

        return ResponseEntity.ok(wrapper);

    }

    @PutMapping(BENCH_ALLOCATION_UPDATE)
    public ResponseEntity<ResponseWrapper<BenchAllocationResponseDTO>> updateBenchAllocation(@Valid @PathVariable Long id, @RequestBody BenchAllocationRequestDTO dto){
        BenchAllocationResponseDTO responseDTO = benchAllocationService.update(id ,dto);

        ResponseWrapper<BenchAllocationResponseDTO> wrapper =
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.SAVED_SUCCESSFULL,
                        responseDTO
                );

        return ResponseEntity.ok(wrapper);

    }
    @PatchMapping (BENCH_ALLOCATION_DEALLOCATE)
    public ResponseEntity<ResponseWrapper<BenchAllocationResponseDTO>> deAlocateEmployee(@Valid @PathVariable Long id, @RequestBody BenchAllocationRequestDTO dto){
        BenchAllocationResponseDTO responseDTO = benchAllocationService.deAllocate(id ,dto);

        ResponseWrapper<BenchAllocationResponseDTO> wrapper =
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.SAVED_SUCCESSFULL,
                        responseDTO
                );

        return ResponseEntity.ok(wrapper);

    }


//mithun is creating a getapi for project allocation

@GetMapping( )
    public ResponseEntity<ResponseWrapper<Object>> Projectemployeeallocation(Pageable pageable){
        Page<BenchAllocationResponseDTO> PReseponseDTO= benchAllocationService.Projectemployeeallocation(pageable);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        PReseponseDTO
                )
        );
}

// api for get by project ID

    @GetMapping(BENCH_GET_BY_PROJECTID)
    public ResponseEntity<ResponseWrapper<Object>> Getbyprojectid(@PathVariable("projectId") Long projectId){
        List<BenchAllocationResponseDTO> responseDTOS =benchAllocationService.Getbyprojectid(projectId);
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTOS

                )

        );

    }

    @GetMapping(BENCH_ALLOCATION_HISTORY_GET_BY_PROJECTID)
    public ResponseEntity<ResponseWrapper<Object>> GetAllocationHistrioybyprojectid(@PathVariable("projectId") Long projectId){

        List<BenchAllocationResponseDTO> responseDTOS =benchAllocationService.GetAllocatiotionHistorybyprojectid(projectId);
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTOS
                )
        );
    }

    // bench filter-mithun
    @GetMapping(BENCH_FILTER)
    public ResponseEntity<ResponseWrapper<Object>> getFilteredBenchAllocations(
            @PathVariable Long projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Long minAvailability,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            Pageable pageable) {

        Page<BenchAllocationResponseDTO> responseDTO = benchAllocationService.getFilteredBenchAllocations(
                projectId,search, roleId, minAvailability, startDateFrom, startDateTo, pageable);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTO
                )
        );
    }
    @GetMapping(BENCH_GET_BY_ID)
    public ResponseEntity<ResponseWrapper<Object>> getBenchAllocationById(
            @PathVariable Long benchAllocationId) {

        BenchAllocationResponseDTO responseDTO = benchAllocationService.getById(benchAllocationId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTO
                )
        );
    }


    @GetMapping("/employee/{empId}/roles")
    public ResponseEntity<List<UserRoleResponseDTO>> getEmployeeRoles(
            @PathVariable Long empId
    ) {
        Set<UserRoleResponseDTO> roles = new HashSet<>();

        // 1. Primary role assigned directly on Employee
        employeeRepository.findById(empId).ifPresent(employee -> {
            if (employee.getRole() != null && employee.getRole().getRoleId() != null) {
                roles.add(new UserRoleResponseDTO(
                        employee.getRole().getRoleId(),
                        employee.getRole().getRoleName()
                ));
            }
        });

        // 2. Active bench project allocation roles
        benchAllocationRepository
                .findCurrentAllocationsByEmployeeId(empId)
                .forEach(allocation -> {
                    if (allocation.getRole() != null && allocation.getRole().getRoleId() != null) {
                        roles.add(new UserRoleResponseDTO(
                                allocation.getRole().getRoleId(),
                                allocation.getRole().getRoleName()
                        ));
                    }
                });

        return ResponseEntity.ok(new ArrayList<>(roles));
    }
    @GetMapping("/project/{projectId}/developers")
    public ResponseEntity<List<BenchAllocationResponseDTO>>
    getCurrentProjectDevelopers(
            @PathVariable Long projectId
    ) {

        return ResponseEntity.ok(
                benchAllocationService
                        .getCurrentProjectDevelopers(projectId)
        );
    }





}