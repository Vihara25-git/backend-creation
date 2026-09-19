package com.sgic.defect_tracker.service.impl;


import com.sgic.defect_tracker.dtos.request.ReleaseViewRequestDto;
import com.sgic.defect_tracker.dtos.response.ReleaseViewResponseDto;
import com.sgic.defect_tracker.dtos.response.RoleResponseDTO;
import com.sgic.defect_tracker.entities.ReleaseType;
import com.sgic.defect_tracker.entities.ReleaseView;
import com.sgic.defect_tracker.mapper.ReleaseViewMapper;
import com.sgic.defect_tracker.repositories.ReleaseTypeRepository;
import com.sgic.defect_tracker.repositories.ReleaseViewRepository;
import com.sgic.defect_tracker.repositories.ReleaseViewRespository;
import com.sgic.defect_tracker.service.ReleaseViewService;
import com.sgic.defect_tracker.utils.ReleaseStatus;
import lombok.RequiredArgsConstructor;
import com.sgic.defect_tracker.dtos.request.ReleaseViewRequestDto;
import org.springframework.stereotype.Service;
import com.sgic.defect_tracker.entities.ProjectDetails;
import com.sgic.defect_tracker.repositories.ProjectDetailsRepository;
import org.springframework.data.domain.PageRequest;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReleaseViewServiceImpl implements ReleaseViewService {

    private final ReleaseViewRepository releaseViewRepository;
   // private final ReleaseViewRespository releaseViewRespository;
   private final ProjectDetailsRepository projectDetailsRepository;
   private final ReleaseTypeRepository releaseTypeRepository;

    private final ReleaseViewMapper releaseViewMapper;

    @Override
    public ReleaseViewResponseDto saveReleaseView(
            ReleaseViewRequestDto requestDto) {

        // Find selected project
        ProjectDetails projectDetails =
                projectDetailsRepository.findById(
                        requestDto.getProjectId()
                ).orElseThrow(() ->
                        new RuntimeException("Project not found")
                );

        Optional<LocalDate> latestReleaseDate =
                releaseViewRepository.findLatestReleaseDateByProjectId(
                        requestDto.getProjectId()
                );

        if (latestReleaseDate.isPresent()
                && !requestDto.getReleaseDate()
                .isAfter(latestReleaseDate.get())) {

            throw new IllegalArgumentException(
                    "Release date must be after the previous release date."
            );
        }

        if (releaseViewRepository.existsByReleaseNameAndProjectDetails_ProjectId(
                requestDto.getReleaseName(),
                requestDto.getProjectId())) {

            throw new IllegalArgumentException(
                    "Release View already exists for this project."
            );
        }



        ReleaseView releaseView =
                releaseViewMapper.toEntity(requestDto);

        ReleaseType releaseType =
                releaseTypeRepository.findById(requestDto.getReleaseTypeId())
                        .orElseThrow(() ->
                                new RuntimeException("Release type not found")
                        );

        releaseView.setReleaseType(releaseType);

//        ProjectDetails projectDetails =
//                projectDetailsRepository.findById(
//                        requestDto.getProjectId()
//                ).orElseThrow(() ->
//                        new RuntimeException("Project not found")
//                );

        releaseView.setProjectDetails(projectDetails);

        ReleaseView savedReleaseView =
                releaseViewRepository.save(releaseView);

        return releaseViewMapper.toDto(savedReleaseView);
    }

    //Update

    @Override
    public ReleaseViewResponseDto getByReleaseId(Long releaseId) {

        ReleaseView releaseview = releaseViewRepository.findById(releaseId)
                .orElseThrow(()-> new RuntimeException("Release view not found"));

        return releaseViewMapper.toDto(releaseview);

    }

    @Override
    public List<ReleaseViewResponseDto> getAllReleaseView(){

        List<ReleaseView> releaseViews = releaseViewRepository.findAll();
        return releaseViewMapper.toDtoList(releaseViews);
    }


    @Override
    public ReleaseViewResponseDto updateReleaseView(
            Long releaseId,
            ReleaseViewRequestDto releaseViewRequestDto) {

        // Find existing release
        ReleaseView releaseView =
                releaseViewRepository.findById(releaseId)
                        .orElseThrow(() ->
                                new RuntimeException("Release view not found")
                        );

        // Get project
        ProjectDetails projectDetails =
                projectDetailsRepository.findById(
                        releaseViewRequestDto.getProjectId()
                ).orElseThrow(() ->
                        new RuntimeException("Project not found")
                );

        LocalDate newReleaseDate =
                releaseViewRequestDto.getReleaseDate();


        // Check duplicate release name
        if (releaseViewRepository
                .existsByReleaseNameAndProjectDetails_ProjectIdAndReleaseIdNot(
                        releaseViewRequestDto.getReleaseName(),
                        releaseViewRequestDto.getProjectId(),
                        releaseId)) {

            throw new IllegalArgumentException(
                    "Release View already exists for this project."
            );
        }


        // Find previous release date
        Optional<LocalDate> previousReleaseDate =
                releaseViewRepository.findPreviousReleaseDate(
                        releaseViewRequestDto.getProjectId(),
                        releaseId
                );


        // Find next release date
        Optional<LocalDate> nextReleaseDate =
                releaseViewRepository.findNextReleaseDate(
                        releaseViewRequestDto.getProjectId(),
                        releaseId
                );


        // Validate against previous release
        if (previousReleaseDate.isPresent()
                && !newReleaseDate.isAfter(previousReleaseDate.get())) {

            throw new IllegalArgumentException(
                    "Release date must be after the previous release date: "
                            + previousReleaseDate.get()
            );
        }


        // Validate against next release
        if (nextReleaseDate.isPresent()
                && !newReleaseDate.isBefore(nextReleaseDate.get())) {

            throw new IllegalArgumentException(
                    "Release date must be before the next release date: "
                            + nextReleaseDate.get()
            );
        }


        // Update release name
        releaseView.setReleaseName(
                releaseViewRequestDto.getReleaseName()
        );


        // Update release version
        releaseView.setReleaseVersion(
                releaseViewRequestDto.getReleaseVersion()
        );


        // Update release date
        releaseView.setReleaseDate(newReleaseDate);


        // Update project
        releaseView.setProjectDetails(projectDetails);


        // Update release type
        ReleaseType releaseType =
                releaseTypeRepository.findById(
                        releaseViewRequestDto.getReleaseTypeId()
                ).orElseThrow(() ->
                        new RuntimeException("Release type not found")
                );

        releaseView.setReleaseType(releaseType);


        // Save updated release
        ReleaseView updatedReleaseView =
                releaseViewRepository.save(releaseView);


        return releaseViewMapper.toDto(updatedReleaseView);
    }

    @Override
    public void deleteReleaseView(Long releaseId) {
        ReleaseView releaseView = releaseViewRepository.findById(releaseId)
                .orElseThrow(() -> new RuntimeException("Release View not found"));

        releaseViewRepository.delete(releaseView);
    }

    // Get release using project id
    @Override
    public List<ReleaseViewResponseDto> getReleaseViewsByProjectId(Long projectId) {

        List<ReleaseView> releaseViews =
                releaseViewRepository.findByProjectDetails_ProjectId(projectId);

        return releaseViewMapper.toDtoList(releaseViews);
    }

    @Override
    public void updateReleaseStatus(Long releaseId, ReleaseStatus status) {

        ReleaseView releaseView =
                releaseViewRepository.findById(releaseId)
                        .orElseThrow(() ->
                                new RuntimeException("Release view not found")
                        );

        releaseView.setStatus(status);

        releaseViewRepository.save(releaseView);
    }
}



