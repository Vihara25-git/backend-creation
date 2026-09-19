package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.TestCaseRequestDTO;
import com.sgic.defect_tracker.dtos.response.TestCaseResponseDTO;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TestCaseService {
    TestCaseResponseDTO createTestCase(TestCaseRequestDTO requestDTO);

    List<TestCaseResponseDTO> filterTestCases(
            String description,
            Long defectTypeId,
            Long projectId,
            Long moduleId,
            Long subModuleId,
            Long severityId
    );


    ResponseWrapper<Page<TestCaseResponseDTO>> getTestCasesBySubModule(
            Long subModuleId,
            Pageable pageable
    );

    //update
    TestCaseResponseDTO updateTestCase(
            Long testCaseId,
            TestCaseRequestDTO testCaseRequestDTO
    );

    //delete
    void deleteTestCase(Long testCaseId);

    // Export test cases to Excel
    byte[] exportTestCases(
            Long projectId,
            Long moduleId,
            Long submoduleId
    );

    //Import test case
    void importTestCases(Long projectId, MultipartFile file);

    //view - project
    ResponseWrapper<Page<TestCaseResponseDTO>> getTestCasesByProject(
            Long projectId,
            Pageable pageable
    );

    // view-project+module

    ResponseWrapper<Page<TestCaseResponseDTO>> getTestCasesByProjectAndModule(
            Long projectId,
            Long moduleId,
            Pageable pageable
    );

}
