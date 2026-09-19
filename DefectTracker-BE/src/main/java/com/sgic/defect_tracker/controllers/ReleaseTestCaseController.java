package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseEmployeeRequestDTO;
import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseRequestDTO;
import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseStatusRequestDTO;
import com.sgic.defect_tracker.dtos.response.*;
import com.sgic.defect_tracker.service.ReleaseTestCaseService;
import com.sgic.defect_tracker.utils.ReleaseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sgic.defect_tracker.dtos.response.QAEmployeeResponseDTO;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/v1/release-test-cases")
@RequiredArgsConstructor
public class ReleaseTestCaseController {

    private final ReleaseTestCaseService releaseTestCaseService;

    @PostMapping("/release/{releaseId}/test-case")
    public ResponseEntity<List<ReleaseTestCaseResponseDTO>> allocateTestCases(
            @PathVariable Long releaseId,
            @RequestBody ReleaseTestCaseRequestDTO request) {

        return ResponseEntity.ok(
                releaseTestCaseService.allocateTestCases(
                        releaseId,
                        request
                )
        );
    }

    @GetMapping("/release/{releaseId}/test-case")
    public ResponseEntity<List<ReleaseTestCaseResponseDTO>> getTestCasesByRelease(
            @PathVariable Long releaseId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long subModuleId) {

        return ResponseEntity.ok(
                releaseTestCaseService.getTestCasesByFilter(
                        releaseId,
                        moduleId,
                        subModuleId
                )
        );
    }

    @GetMapping("/release/{releaseId}/test-case/{id}")
    public ResponseEntity<ReleaseTestCaseResponseDTO> getReleaseTestCase(
            @PathVariable Long releaseId,
            @PathVariable Long id) {

        return ResponseEntity.ok(
                releaseTestCaseService.getReleaseTestCase(
                        releaseId,
                        id
                )
        );
    }
    @GetMapping("/release/{releaseId}/test-case/{testcaseId}/employees")
    public ResponseEntity<List<QAEmployeeResponseDTO>> getAvailableQAEmployees(
            @PathVariable Long releaseId,
            @PathVariable Long testcaseId) {

        return ResponseEntity.ok(
                releaseTestCaseService.getAvailableQAEmployees(
                        releaseId,
                        testcaseId
                )
        );
    }

    @PostMapping(
            "/release/{releaseId}/test-case/{testcaseId}/employee"
    )
    public ResponseEntity<ReleaseTestCaseResponseDTO> assignQAEmployee(
            @PathVariable Long releaseId,
            @PathVariable Long testcaseId,
            @RequestBody ReleaseTestCaseEmployeeRequestDTO request) {

        return ResponseEntity.ok(
                releaseTestCaseService.assignQAEmployee(
                        releaseId,
                        testcaseId,
                        request
                )
        );
    }

    @PatchMapping(
            value = "/release/{releaseId}/test-case/{releaseTestCaseId}/status",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ReleaseTestCaseResponseDTO> updateStatus(
            @PathVariable Long releaseId,
            @PathVariable Long releaseTestCaseId,
            @RequestPart("data") ReleaseTestCaseStatusRequestDTO request,
            @RequestPart(value = "attachmentFile", required = false)
            MultipartFile attachmentFile) {

        return ResponseEntity.ok(
                releaseTestCaseService.updateStatus(
                        releaseId,
                        releaseTestCaseId,
                        request,
                        attachmentFile
                )
        );
    }

    @PatchMapping("/release/{releaseId}/status")
    public ResponseEntity<?> updateReleaseStatus(
            @PathVariable Long releaseId,
            @RequestBody ReleaseStatus request) {

        releaseTestCaseService.updateReleaseStatus(
                releaseId,
                request
        );

        return ResponseEntity.ok().build();
    }


    @GetMapping("/release/{releaseId}/test-case/count")
    public ResponseEntity<ReleaseTestCaseCountResponseDto> getTestCaseCount(
            @PathVariable Long releaseId) {

        return ResponseEntity.ok(
                releaseTestCaseService.getTestCaseCount(releaseId)
        );
    }

//    @GetMapping("/release/{releaseId}/available-test-cases")
//    public ResponseEntity<List<TestCaseResponseDTO>> getAvailableTestCases(
//            @PathVariable Long releaseId,
//            @RequestParam Long projectId,
//            @RequestParam Long moduleId,
//            @RequestParam Long subModuleId) {
//
//        return ResponseEntity.ok(
//                releaseTestCaseService.getAvailableTestCases(
//                        releaseId,
//                        projectId,
//                        moduleId,
//                        subModuleId
//                )
//        );
//    }

    @GetMapping("/release/{releaseId}/available-test-cases")
    public ResponseEntity<List<TestCaseResponseDTO>> getAvailableTestCases(
            @PathVariable Long releaseId,
            @RequestParam Long projectId,
            @RequestParam Long moduleId,
            @RequestParam Long subModuleId) {

        return ResponseEntity.ok(
                releaseTestCaseService.getAvailableTestCases(
                        releaseId,
                        projectId,
                        moduleId,
                        subModuleId
                )
        );
    }

    @DeleteMapping("/release/{releaseId}/test-case/{releaseTestCaseId}")
    public ResponseEntity<Void> deleteReleaseTestCase(
            @PathVariable Long releaseId,
            @PathVariable Long releaseTestCaseId) {

        releaseTestCaseService.deleteReleaseTestCase(releaseId, releaseTestCaseId);
        return ResponseEntity.noContent().build();
    }

}
