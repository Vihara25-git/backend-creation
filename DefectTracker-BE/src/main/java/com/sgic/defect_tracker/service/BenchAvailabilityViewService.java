package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.BenchAvailabilityViewFilterDTO;
import com.sgic.defect_tracker.dtos.response.BenchAvailabilityViewResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface BenchAvailabilityViewService {

    Page<BenchAvailabilityViewResponseDTO> getBenchAvailabilityView(Pageable pageable);
    Page<BenchAvailabilityViewResponseDTO> filterBenchAvailabilityView(
            BenchAvailabilityViewFilterDTO dto,
            Pageable pageable
    );
    BenchAvailabilityViewResponseDTO getemployeeBenchAvailabilityView(Long empId);
}