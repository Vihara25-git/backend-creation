package com.sgic.defect_tracker.service.impl;
import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.entities.BenchAvailabilityView;
import com.sgic.defect_tracker.entities.Defect;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.utils.ValidationMessages;
import com.sgic.defect_tracker.dtos.request.BenchAvailabilityViewFilterDTO;
import com.sgic.defect_tracker.specification.BenchAvailabilityViewSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.sgic.defect_tracker.dtos.response.BenchAvailabilityViewResponseDTO;
import com.sgic.defect_tracker.mapper.BenchAvailabilityViewMapper;
import com.sgic.defect_tracker.repositories.BenchAvailabilityViewRepository;
import com.sgic.defect_tracker.service.BenchAvailabilityViewService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BenchAvailabilityViewServiceImpl implements BenchAvailabilityViewService {

    @Autowired
    private BenchAvailabilityViewRepository repository;
    @Autowired
    private  BenchAvailabilityViewMapper mapper;


    @Override
    public Page<BenchAvailabilityViewResponseDTO> getBenchAvailabilityView(
            Pageable pageable) {

        return repository.findByEmpIdNot(1L, pageable)
                .map(mapper::toResponse);
    }
    @Override
    public Page<BenchAvailabilityViewResponseDTO> filterBenchAvailabilityView(
            BenchAvailabilityViewFilterDTO dto,
            Pageable pageable) {

        return repository
                .findAll(
                        BenchAvailabilityViewSpecification.filter(dto),
                        pageable
                )
                .map(mapper::toResponse);
    }

    @Override
    public BenchAvailabilityViewResponseDTO getemployeeBenchAvailabilityView(Long empId){
        BenchAvailabilityView allocations = repository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ValidationMessages.INVALID_ID + empId));



        return mapper.toResponse(allocations);

    }
}
