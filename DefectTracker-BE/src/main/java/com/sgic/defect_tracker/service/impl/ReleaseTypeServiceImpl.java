package com.sgic.defect_tracker.service.impl;


import com.sgic.defect_tracker.dtos.request.ReleaseTypeRequestDTO;
import com.sgic.defect_tracker.dtos.response.ReleaseTypeResponseDTO;
import com.sgic.defect_tracker.entities.ReleaseType;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.repositories.ReleaseTypeRepository;
import com.sgic.defect_tracker.service.ReleaseTypeService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ReleaseTypeServiceImpl implements ReleaseTypeService {

    private final ReleaseTypeRepository releaseTypeRepository;

    public ReleaseTypeServiceImpl(ReleaseTypeRepository releaseTypeRepository) {
        this.releaseTypeRepository = releaseTypeRepository;
    }


    @Override
    public ReleaseTypeResponseDTO createReleaseType(ReleaseTypeRequestDTO requestDTO) {
        if (releaseTypeRepository.existsByReleaseTypeNameIgnoreCase(requestDTO.getType())) {
            throw new IllegalArgumentException("Release type with name '" + requestDTO.getType() + "' already exists.");
        }
        ReleaseType releaseType = new ReleaseType();
        releaseType.setType(requestDTO.getType());

        ReleaseType savedReleaseType = releaseTypeRepository.save(releaseType);
        return mapToResponseDTO(savedReleaseType);
    }

    @Override
    public ReleaseTypeResponseDTO updateReleaseType(Long id, ReleaseTypeRequestDTO requestDTO) {
        ReleaseType existingReleaseType = releaseTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release type not found with ID: " + id));

        existingReleaseType.setType(requestDTO.getType());
        ReleaseType updatedReleaseType = releaseTypeRepository.save(existingReleaseType);
        return mapToResponseDTO(updatedReleaseType);
    }

    @Override
    public Page<ReleaseTypeResponseDTO> getAllReleaseTypes(Pageable pageable) {
        return releaseTypeRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public ReleaseTypeResponseDTO getReleaseTypeById(Long id) {
        ReleaseType releaseType = releaseTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release type not found with ID: " + id));
        return mapToResponseDTO(releaseType);
    }

    @Override
    public void deleteReleaseType(Long id) {
        ReleaseType releaseType = releaseTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Release type not found with ID: " + id));
        releaseTypeRepository.delete(releaseType);
    }

    private ReleaseTypeResponseDTO mapToResponseDTO(ReleaseType releaseType) {
        return ReleaseTypeResponseDTO.builder()
                .id(releaseType.getId())
                .type(releaseType.getType())
                .build();
    }
}
