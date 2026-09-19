package com.sgic.defect_tracker.service.impl;
import com.sgic.defect_tracker.dtos.request.DesignationRequestDTO;
import com.sgic.defect_tracker.dtos.response.RoleResponseDTO;
import com.sgic.defect_tracker.entities.Designation;
import com.sgic.defect_tracker.dtos.response.DesignationResponseDTO;
import com.sgic.defect_tracker.entities.Designation;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.mapper.DesignationMapper;
import com.sgic.defect_tracker.repositories.DesignationRepository;
import com.sgic.defect_tracker.service.DesignationService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignationServiceImplementation implements DesignationService {


    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private DesignationMapper designationMapper;


    //Post
    @Override
    public DesignationResponseDTO createDesignation(DesignationRequestDTO requestDTO) {

        String name = requestDTO.getDesignationName();

        //  Required
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Designation name is required."
            );
        }

        //  Maximum 25 characters
        if (name.length() > 25) {
            throw new IllegalArgumentException(
                    "Designation name must not exceed 25 characters."
            );
        }

        //  Check spaces at beginning/end
        if (!name.equals(name.trim())) {
            throw new IllegalArgumentException(
                    "Designation name must not start or end with a space."
            );
        }

        //  Check multiple spaces
        if (name.contains("  ")) {
            throw new IllegalArgumentException(
                    "Designation name must contain only single spaces between words."
            );
        }

        // Check only letters and single spaces
        if (!name.matches("[A-Za-z]+( [A-Za-z]+)*")) {
            throw new IllegalArgumentException(
                    "Designation name must contain only letters."
            );
        }

        // Check first letter of every word is capital
        if (!Character.isUpperCase(name.charAt(0))) {
            throw new IllegalArgumentException(
                    "Designation name must start with a capital letter."
            );
        }

        // Check duplicate designation
        if (designationRepository.existsByNormalizedDesignationName(name)) {
            throw new IllegalArgumentException(
                    "Designation '" + name + "' is already in use."
            );
        }

        // 6. Save
        Designation designation =
                designationMapper.toEntity(requestDTO);

        Designation savedDesignation =
                designationRepository.save(designation);

        return designationMapper.toDto(savedDesignation);
    }

    //Get All
    @Override
    public Page<DesignationResponseDTO> getAllDesignation(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Designation> designationPage =
                designationRepository.findByDesignationIdNot(1L, pageable);

        List<DesignationResponseDTO> dtoList =
                designationMapper.toDtoList(designationPage.getContent());

        return new PageImpl<>(
                dtoList,
                pageable,
                designationPage.getTotalElements()
        );
    }


    // Update
    @Override
    public DesignationResponseDTO updateDesignation(
            Long designationId,
            DesignationRequestDTO requestDTO) {

        // designation by id
        Designation designation =
                designationRepository.findById(designationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Designation not found with id:" + designationId
                                ));

        // new designation name
        String newName = requestDTO.getDesignationName();

        // Check empty name
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException(
                    "Designation name is Required."
            );
        }

        // Maximum 25 characters
        if (newName.length() > 25) {
            throw new IllegalArgumentException(
                    "Designation name must not exceed 25 characters."
            );
        }

        // Start & End Space
        if (!newName.equals(newName.trim())) {
            throw new IllegalArgumentException(
                    "Designation name must not start or end with a space."
            );
        }

        // Multiple spaces
        if (newName.contains("  ")) {
            throw new IllegalArgumentException(
                    "Designation name must contain only single spaces between words."
            );
        }

        // Only letters & single spaces
        if (!newName.matches("[A-Za-z]+( [A-Za-z]+)*")) {
            throw new IllegalArgumentException(
                    "Designation name must contain only letters and one space between words."
            );
        }

        // ONLY FIRST LETTER must be capital
        if (!Character.isUpperCase(newName.charAt(0))) {
            throw new IllegalArgumentException(
                    "Designation name must start with a capital letter."
            );
        }

        // Same existing designation name
        if (designation.getDesignationName().equals(newName)) {
            throw new IllegalArgumentException(
                    "No changes were made to the designation."
            );
        }

        // Duplicate designation
        if (designationRepository.existsByNormalizedDesignationName(newName)) {
            throw new IllegalArgumentException(
                    "Designation '" + newName + "' is already in use."
            );
        }

        // Update designation
        designationMapper.updateDesignationFromDto(
                requestDTO,
                designation
        );

        // Save designation
        Designation updatedDesignation =
                designationRepository.save(designation);

        // Return response DTO
        return designationMapper.toDto(updatedDesignation);
    }

    @Override
    public DesignationResponseDTO getByDesignationId(Long designationId) {

        Designation designation = designationRepository.findById(designationId)
                .orElseThrow(()-> new RuntimeException("Designation not found"));

        return designationMapper.toDto(designation);
    }


    @Override
    public void deleteDesignation(Long designationId){
        Designation designation = designationRepository.findById(designationId)
                .orElseThrow(()->new RuntimeException("Designation not found"));

        designationRepository.delete(designation);
    }


}


