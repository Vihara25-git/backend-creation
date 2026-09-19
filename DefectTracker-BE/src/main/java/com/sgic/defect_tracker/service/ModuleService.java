package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.ModuleRequestDto;
import com.sgic.defect_tracker.dtos.response.ModuleResponseDto;
import com.sgic.defect_tracker.entities.ProjectDetails;

import java.util.List;

public interface ModuleService {
    List<ModuleResponseDto> getAllModule(Long projectId,String name);
    //Create
    ModuleResponseDto createModule(Long projectId, ModuleRequestDto moduleRequestDto);
   // ModuleResponseDto createModule(ModuleRequestDto moduleRequestDto);

    //Update
    ModuleResponseDto updateModule(Long moduleId, ModuleRequestDto requestDto);
    void  deleteModule(Long moduleId);

    ModuleResponseDto getModuleById(Long moduleId);
}
