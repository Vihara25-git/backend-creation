package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.ReleaseViewRequestDto;
import com.sgic.defect_tracker.dtos.request.RoleRequestDTO;
import com.sgic.defect_tracker.dtos.response.ReleaseViewResponseDto;
import com.sgic.defect_tracker.entities.ReleaseView;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.ReleaseViewService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ReleaseStatus;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(EndpointBundle.ReleaseView)
@RequiredArgsConstructor
public class ReleaseViewController {

    private final ReleaseViewService releaseViewService;

    //create releaseView
    @PostMapping("/save")
    public ResponseEntity<ResponseWrapper<ReleaseViewResponseDto>> saveReleaseView(
            @RequestBody ReleaseViewRequestDto releaseViewRequestDto) {

        ReleaseViewResponseDto releaseViewResponseDto = releaseViewService.saveReleaseView(releaseViewRequestDto);


        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Release view create successfully.",
                        releaseViewResponseDto
                )
        );

    }


    //Update ReleaseView
    @PutMapping("/update/{ReleaseId}")
    public ResponseEntity<ResponseWrapper<Object>> updateReleaseView(
            @PathVariable("ReleaseId") Long releaseId,
            @RequestBody ReleaseViewRequestDto releaseViewRequestDto)

    {

          ReleaseViewResponseDto releaseViewResponseDto= releaseViewService.updateReleaseView(releaseId, releaseViewRequestDto);
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.UPDATED.getCode(),
                        "ReleaseView Updated Successfully.",
                        releaseViewResponseDto
                )
        );

    }

    //Get ReleaseView
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ReleaseViewResponseDto>>> getAllReleaseView() {
        List<ReleaseViewResponseDto> response = releaseViewService.getAllReleaseView();

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Release view retrieved successfully..",
                        response
                )
        );

    }

    //Get ReleaseView By Id
    @GetMapping("/view/{releaseId}")
    public ResponseEntity<ResponseWrapper<ReleaseViewResponseDto>>getAllReleaseId(@PathVariable Long releaseId){


        ReleaseViewResponseDto response = releaseViewService.getByReleaseId(releaseId);

        return ResponseEntity.ok(new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Release View retrieved successfully.",
                        response
                )
        );


    }


    //get using project id
    // Get ReleaseViews by Project ID
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ResponseWrapper<List<ReleaseViewResponseDto>>> getReleaseViewsByProjectId(
            @PathVariable Long projectId) {

        List<ReleaseViewResponseDto> response = releaseViewService.getReleaseViewsByProjectId(projectId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Release views retrieved successfully.",
                        response
                )
        );
    }

    @DeleteMapping("/delete/{releaseId}")
    public ResponseEntity<ResponseWrapper<String>> deleteReleaseView(
            @PathVariable Long releaseId ) {

        releaseViewService.deleteReleaseView(releaseId);


        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "Release view delete successfully.",
                        "success"
                )
        );
    }

    @PutMapping("/{releaseId}/status")
    public ResponseEntity<?> updateReleaseStatus(
            @PathVariable Long releaseId,
            @RequestParam ReleaseStatus status) {

        releaseViewService.updateReleaseStatus(
                releaseId,
                status
        );

        return ResponseEntity.ok(
                "Release status updated successfully"
        );
    }

    }

