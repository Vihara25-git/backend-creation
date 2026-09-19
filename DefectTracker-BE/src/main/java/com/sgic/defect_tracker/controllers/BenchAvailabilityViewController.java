package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.BenchAvailabilityViewFilterDTO;
import com.sgic.defect_tracker.dtos.request.DefectFilterDTO;
import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.dtos.response.BenchAvailabilityViewResponseDTO;
import com.sgic.defect_tracker.entities.BenchAvailabilityView;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.BenchAvailabilityViewService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.utils.ValidationMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;

import static com.sgic.defect_tracker.utils.EndpointBundle.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping(BENCH_AVAILABILITY)
@RequiredArgsConstructor
public class BenchAvailabilityViewController {
    @Autowired
    private final BenchAvailabilityViewService benchAvailabilityViewService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<Object>> getBenchAvailabilityView(
            Pageable pageable){
        Page<BenchAvailabilityViewResponseDTO> responseDTO =
                            benchAvailabilityViewService.getBenchAvailabilityView(pageable);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTO
                )
        );
    }


    @GetMapping("/filter")
    public ResponseEntity<ResponseWrapper<Object>> filterBenchAvailability(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String designationName,
            @RequestParam(required = false) Long availablePercentage,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Pageable pageable) {

        BenchAvailabilityViewFilterDTO dto =
                new BenchAvailabilityViewFilterDTO();

        dto.setSearch(search);
        dto.setDesignationName(designationName);
        dto.setAvailablePercentage(availablePercentage);

        if (startDate != null && !startDate.isBlank()) {
            dto.setStartDate(
                    java.time.LocalDate.parse(startDate)
            );
        }

        if (endDate != null && !endDate.isBlank()) {
            dto.setEndDate(
                    java.time.LocalDate.parse(endDate)
            );
        }

        Page<BenchAvailabilityViewResponseDTO> responseDTO =
                benchAvailabilityViewService
                        .filterBenchAvailabilityView(dto, pageable);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTO
                )
        );
    }
    @GetMapping(BENCH_EMPLOYEE)
    public ResponseEntity<ResponseWrapper<Object>> getemployebenchavailability(@PathVariable("empId") Long empId){

        BenchAvailabilityViewResponseDTO responseDTOS =benchAvailabilityViewService.getemployeeBenchAvailabilityView(empId);

        return ResponseEntity.ok(

                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        ValidationMessages.RETRIEVED,
                        responseDTOS

                )

        );

    }


}
