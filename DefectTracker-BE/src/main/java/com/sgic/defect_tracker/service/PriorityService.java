package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.PriorityRequestDto;
import com.sgic.defect_tracker.dtos.response.PriorityResponseDto;
import org.springframework.data.domain.Page;



import java.util.List;

public interface PriorityService {
    PriorityResponseDto updatePriority(Long id, PriorityRequestDto requestDto);

    Page<PriorityResponseDto> getAllPriorities(int page, int size);

    PriorityResponseDto createPriority(PriorityRequestDto requestDto);
    void deletePriority(Long id);
}
