package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.PriorityRequestDto;
import com.sgic.defect_tracker.dtos.response.PriorityResponseDto;
import com.sgic.defect_tracker.entities.Priority;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.mapper.PriorityMapper;
import com.sgic.defect_tracker.repositories.PriorityRepository;
import com.sgic.defect_tracker.service.PriorityService;
import com.sgic.defect_tracker.utils.ValidationMessages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class PriorityServiceImpl implements PriorityService {

    private final PriorityRepository priorityRepository;
    private final PriorityMapper priorityMapper;

    public PriorityServiceImpl(PriorityRepository priorityRepository, PriorityMapper priorityMapper) {
        this.priorityRepository = priorityRepository;
        this.priorityMapper = priorityMapper;
    }

    @Override
    public PriorityResponseDto updatePriority(Long id, PriorityRequestDto requestDto) {

        Priority priority = priorityRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Priority not found"));

        String newName = requestDto.getPriorityName().trim();
        String newColor = requestDto.getColorCode().trim();
        boolean nochanges = priority.getPriorityName().equals(requestDto.getPriorityName())
                && priority.getColorCode().equals(requestDto.getColorCode());

        if (nochanges) {
            throw new IllegalArgumentException("No changes detected. Priority is already up to date.");
        }

        Priority existingPriority =
                priorityRepository.findByPriorityNameIgnoreCase(newName);

        if (existingPriority != null
                && !existingPriority.getPriorityId().equals(id)) {

            throw new IllegalArgumentException(
                    "Priority Name already exists."
            );
        }

        Priority existingColor =
                priorityRepository.findByColorCodeIgnoreCase(newColor);

        if (existingColor != null
                && !existingColor.getPriorityId().equals(id)) {

            throw new IllegalArgumentException(
                    "This color is already in use. Please choose a different color."
            );
        }

        requestDto.setPriorityName(newName);
        requestDto.setColorCode(newColor);
        priorityMapper.updateEntityFromDto(requestDto, priority);

        Priority updatedPriority = priorityRepository.save(priority);

        return priorityMapper.toDto(updatedPriority);
    }

    @Override
    public Page<PriorityResponseDto> getAllPriorities(int page,int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Priority> priorities = priorityRepository.findAll(pageable);

        if(priorities.isEmpty()) {
            throw new ResourceNotFoundException("No priorities found");
        }

        return priorities.map(priorityMapper::toDto);

    }

    @Override
    public PriorityResponseDto createPriority(PriorityRequestDto requestDto) {


        if (requestDto.getPriorityName() == null ||
                requestDto.getPriorityName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    ValidationMessages.REQUIRED
            );
        }


        if (requestDto.getPriorityName().length() < 2 ||
                requestDto.getPriorityName().length() > 20) {

            throw new IllegalArgumentException(
                    ValidationMessages.NAME_LENGTH
            );
        }


        if (requestDto.getColorCode() == null ||
                requestDto.getColorCode().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    ValidationMessages.REQUIRED
            );
        }


        boolean exists = priorityRepository.findAll()
                .stream()
                .anyMatch(priority ->
                        priority.getPriorityName().trim()
                                .equalsIgnoreCase(requestDto.getPriorityName().trim())
                );

        if (exists) {
            throw new IllegalArgumentException(
                    "This Priority Already Exists !!!! !"
            );
        }
        boolean color = priorityRepository.findAll()
                .stream()
                .anyMatch(priority ->
                        priority.getColorCode()
                                .equalsIgnoreCase(requestDto.getPriorityName())
                );

        if (color) {
            throw new IllegalArgumentException(
                    "This Color Already Exists !!!! !"
            );
        }



        Priority priority = priorityMapper.toEntity(requestDto);

        Priority savedPriority = priorityRepository.save(priority);

        return priorityMapper.toDto(savedPriority);
    }


    @Override
    public void deletePriority(Long id) {

        Priority priority = priorityRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Priority Not Found"));

        priorityRepository.delete(priority);
    }
}
