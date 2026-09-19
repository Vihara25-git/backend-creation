package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.BenchAllocationRequestDTO;
import com.sgic.defect_tracker.dtos.response.BenchAllocationResponseDTO;
import com.sgic.defect_tracker.entities.BenchAllocation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BenchAllocationService {

   // List<BenchAllocation> getAllBenchAllocations();

    //BenchAllocation saveBenchAllocation(BenchAllocation benchAllocation);

    BenchAllocationResponseDTO update(Long benchAllocationId, BenchAllocationRequestDTO request);

    BenchAllocationResponseDTO save(@Valid BenchAllocationRequestDTO dto);


    Page<BenchAllocationResponseDTO> Projectemployeeallocation(Pageable pageable);

    List<BenchAllocationResponseDTO> Getbyprojectid(Long projectId);

    BenchAllocationResponseDTO deAllocate(Long benchAllocationId, BenchAllocationRequestDTO requestDTO);

    BenchAllocationResponseDTO getById(Long benchAllocationId);

    Page<BenchAllocationResponseDTO> getFilteredBenchAllocations(
            Long projectId,
            String search,
            Long designationId,
            Long minAvailability,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            Pageable pageable
    );


    List<BenchAllocationResponseDTO> GetAllocatiotionHistorybyprojectid(Long projectId);

    List<BenchAllocationResponseDTO> getCurrentProjectDevelopers(
            Long projectId
    );


}