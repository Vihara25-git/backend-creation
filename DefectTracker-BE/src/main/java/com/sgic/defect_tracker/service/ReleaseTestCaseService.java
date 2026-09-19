package com.sgic.defect_tracker.service;



import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseEmployeeRequestDTO;
import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseRequestDTO;
import com.sgic.defect_tracker.dtos.request.ReleaseTestCaseStatusRequestDTO;
import com.sgic.defect_tracker.dtos.response.QAEmployeeResponseDTO;
import com.sgic.defect_tracker.dtos.response.ReleaseTestCaseResponseDTO;
import com.sgic.defect_tracker.dtos.response.TestCaseResponseDTO;
import com.sgic.defect_tracker.utils.ReleaseStatus;
import org.springframework.web.multipart.MultipartFile;
import com.sgic.defect_tracker.dtos.response.ReleaseTestCaseCountResponseDto;

import java.util.List;

public interface ReleaseTestCaseService {

    List<ReleaseTestCaseResponseDTO> allocateTestCases(
            Long releaseId,
            ReleaseTestCaseRequestDTO request
    );

    List<ReleaseTestCaseResponseDTO> getTestCasesByFilter(
            Long releaseId,
            Long moduleId,
            Long subModuleId
    );

    ReleaseTestCaseResponseDTO getReleaseTestCase(
            Long releaseId,
            Long releaseTestCaseId
    );

    List<QAEmployeeResponseDTO> getAvailableQAEmployees(
            Long releaseId,
            Long testcaseId
    );

    ReleaseTestCaseResponseDTO assignQAEmployee(
            Long releaseId,
            Long testcaseId,
            ReleaseTestCaseEmployeeRequestDTO request
    );

    ReleaseTestCaseResponseDTO updateStatus(
            Long releaseId,
            Long releaseTestCaseId,
            ReleaseTestCaseStatusRequestDTO request,
            MultipartFile attachmentFile
    );

    ReleaseTestCaseCountResponseDto getTestCaseCount(Long releaseId);

    void updateReleaseStatus(
            Long releaseId,
            ReleaseStatus status
    );

    //Filter Allocate Testcase for Release
    List<TestCaseResponseDTO> getAvailableTestCases(
            Long releaseId,
            Long projectId,
            Long moduleId,
            Long subModuleId
    );

    void deleteReleaseTestCase(Long releaseId, Long releaseTestCaseId);
}