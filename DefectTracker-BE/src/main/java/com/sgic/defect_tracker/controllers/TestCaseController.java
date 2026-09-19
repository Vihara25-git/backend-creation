package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.TestCaseRequestDTO;
import com.sgic.defect_tracker.dtos.response.TestCaseResponseDTO;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.TestCaseService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;
import java.util.List;


@RestController
@RequestMapping(EndpointBundle.TEST_CASE)
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    //update Test_Case
    @PutMapping({EndpointBundle.TEST_CASE_ALL + EndpointBundle.TEST_CASE_BY_ID, "/testcase/{testCaseId}"})
    public ResponseEntity<ResponseWrapper<Object>> updateTestCase(
            @PathVariable("testCaseId") Long testCaseId,
             @Valid @RequestBody TestCaseRequestDTO testCaseRequestDTO
    )
    {
        TestCaseResponseDTO testCaseResponseDTO = testCaseService.updateTestCase(testCaseId, testCaseRequestDTO);
        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.UPDATED.getCode(),
                        "Test Case Updated Successfully.",
                        testCaseResponseDTO
                )
        );
    }

    @PostMapping(EndpointBundle.TEST_CASE_ALL)
    public ResponseEntity<ResponseWrapper<TestCaseResponseDTO>> createTestCase(
            @PathVariable("projectId") Long projectId,
            @PathVariable("moduleId") Long moduleId,
            @PathVariable("submoduleId") Long submoduleId,
            @Valid @RequestBody TestCaseRequestDTO requestDTO) {

        if (requestDTO.getProjectId() == null) {
            requestDTO.setProjectId(projectId);
        }
        if (requestDTO.getModuleId() == null) {
            requestDTO.setModuleId(moduleId);
        }
        if (requestDTO.getSubModuleId() == null) {
            requestDTO.setSubModuleId(submoduleId);
        }

        TestCaseResponseDTO response =
                testCaseService.createTestCase(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.CREATED.getCode(),
                        "Test Case created successfully",
                        response
                )
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TestCaseResponseDTO>> filterTestCases(
            @PathVariable(value = "projectId", required = false) Long pathProjectId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long defectTypeId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long subModuleId,
            @RequestParam(required = false) Long severityId) {

        Long effectiveProjectId = projectId != null ? projectId : pathProjectId;

        List<TestCaseResponseDTO> response =
                testCaseService.filterTestCases(
                        description,
                        defectTypeId,
                        effectiveProjectId,
                        moduleId,
                        subModuleId,
                        severityId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping(EndpointBundle.TEST_CASE_ALL)
    public ResponseEntity<ResponseWrapper<Page<TestCaseResponseDTO>>> getTestCases(
            @PathVariable("projectId") Long projectId,
            @PathVariable("moduleId") Long moduleId,
            @PathVariable("submoduleId") Long submoduleId,
            Pageable pageable) {

        return ResponseEntity.ok(
                testCaseService.getTestCasesBySubModule(
                        submoduleId,
                        pageable
                )
        );
    }

    //delete
    @DeleteMapping({EndpointBundle.TEST_CASE_ALL + EndpointBundle.TEST_CASE_BY_ID, "/testcase/{testCaseId}"})
    public ResponseEntity<Void> deleteTestCase(
            @PathVariable Long testCaseId) {

        testCaseService.deleteTestCase(testCaseId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/testcase")
    public ResponseEntity<ResponseWrapper<Page<TestCaseResponseDTO>>> getTestCasesByProject(
            @PathVariable("projectId") Long projectId,
            Pageable pageable) {

        return ResponseEntity.ok(
                testCaseService.getTestCasesByProject(
                        projectId,
                        pageable
                )
        );
    }

    @GetMapping("/module/{moduleId}/testcase")
    public ResponseEntity<ResponseWrapper<Page<TestCaseResponseDTO>>> getTestCasesByProjectAndModule(
            @PathVariable("projectId") Long projectId,
            @PathVariable("moduleId") Long moduleId,
            Pageable pageable) {

        return ResponseEntity.ok(
                testCaseService.getTestCasesByProjectAndModule(
                        projectId,
                        moduleId,
                        pageable
                )
        );
    }

    //export testcase
    @GetMapping("/bulk-export")
    public ResponseEntity<Resource> exportTestCases(
            @PathVariable("projectId") Long projectId,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long submoduleId) {

        byte[] file = testCaseService.exportTestCases(
                projectId,
                moduleId,
                submoduleId
        );

        ByteArrayResource resource = new ByteArrayResource(file);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(
                        "Content-Disposition",
                        "attachment; filename=testcases_project_" + projectId + ".xlsx"
                )
                .body(resource);
    }

    // IMPORT TEST CASES FROM EXCEL
    @PostMapping("/bulk-import")
    public ResponseEntity<?> importTestCases(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file) {

        testCaseService.importTestCases(projectId, file);

        return ResponseEntity.ok(
                "Test cases imported successfully"
        );
    }
}
