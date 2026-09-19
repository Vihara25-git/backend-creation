package com.sgic.defect_tracker.service.impl;


//import com.sgic.defect_tracker.dtos.DefectDto;

import com.sgic.defect_tracker.dtos.request.DefectTypeRequestDto;
import com.sgic.defect_tracker.dtos.response.DefectTypeResponseDto;
import com.sgic.defect_tracker.entities.DefectType;
import com.sgic.defect_tracker.exceptionHandlers.IllegalArgumentException;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.mapper.DefectTypemapper;
import com.sgic.defect_tracker.repositories.DefectTypeRepository;
import com.sgic.defect_tracker.service.DefectTypeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

import static com.sgic.defect_tracker.utils.ValidationMessages.*;

@Service
public class DefectTypeServiceImplementation implements DefectTypeService {
    @Autowired
    private DefectTypeRepository defectTypeRepository;
    @Autowired
    private DefectTypemapper defectTypemapper;


    @Override
    public DefectTypeResponseDto defectsave(DefectTypeRequestDto defect) {

        if(defect == null || defect.getDefectTypeName() == null
                || defect.getDefectTypeName().isBlank()){
            throw new IllegalArgumentException(INVALID_ID);
        }

        String name = normalizeName(defect.getDefectTypeName());

        if(name.isBlank()){
            throw new IllegalArgumentException(INVALID_ID);
        }

        boolean duplicate = defectTypeRepository.findAll().stream()
                .anyMatch(existing ->
                        normalizeName(existing.getDefectTypeName())
                                .equalsIgnoreCase(name));

        if(duplicate){
            throw new IllegalArgumentException(DUPLICATE_ENTRY);
        }

        DefectType defectType = defectTypemapper.toDefectEntity(defect);
        defectType.setDefectTypeName(name);

        return defectTypemapper.toDefectDto(
                defectTypeRepository.save(defectType)
        );
    }

    private String normalizeName(String raw) {
        return raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
    }

    @Override
    public Page<DefectTypeResponseDto> defectgetall(Pageable pageable) {
        Page<DefectType> defects = defectTypeRepository.findAll(pageable);
        if (defects.isEmpty()) {
            throw new ResourceNotFoundException(NOT_FOUND);
        }

        return defects.map(defectTypemapper::toDefectDto);

    }

    @Override
    public String defectdelete(Long id) {

        Optional<DefectType> defectType = defectTypeRepository.findById(id);


        if (defectType.isEmpty()) {
            throw new ResourceNotFoundException(NOT_FOUND);
        }
        defectTypeRepository.deleteById(id);
        return DELETE_SUCCESS;
    }


    @Override
    public DefectTypeResponseDto updatedefect(DefectTypeRequestDto defect, Long defectTypeId) {

        if(defect == null || defect.getDefectTypeName() == null
                || defect.getDefectTypeName().isBlank()){
            throw new ResourceNotFoundException("NOT_FOUND");
        }

        DefectType defectexist = defectTypeRepository.findById(defectTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("DefectType not found"));

        String name = normalizeName(defect.getDefectTypeName());

        if(name.isBlank()){
            throw new IllegalArgumentException(INVALID_ID);
        }

        boolean duplicate = defectTypeRepository.findAll().stream()
                .filter(existing -> !existing.getDefectTypeId().equals(defectTypeId))
                .anyMatch(existing ->
                        normalizeName(existing.getDefectTypeName())
                                .equalsIgnoreCase(name));

        if(duplicate){
            throw new IllegalArgumentException(DUPLICATE_ENTRY);
        }

        defectexist.setDefectTypeName(name);



        DefectType updatedDefect = defectTypeRepository.save(defectexist);


        return defectTypemapper.toDefectDto(updatedDefect);
    }



}