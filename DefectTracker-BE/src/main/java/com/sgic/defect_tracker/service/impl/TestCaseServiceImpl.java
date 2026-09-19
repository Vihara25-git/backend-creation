package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.TestCaseRequestDTO;
import com.sgic.defect_tracker.dtos.response.TestCaseResponseDTO;
import com.sgic.defect_tracker.entities.*;
import com.sgic.defect_tracker.entities.Module;
import com.sgic.defect_tracker.exceptions.DuplicateResourceException;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.mapper.TestCaseMapper;
import com.sgic.defect_tracker.repositories.*;
import com.sgic.defect_tracker.service.TestCaseService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final ProjectDetailsRepository projectDetailsRepository;
    private final ModuleRepositories moduleRepositories;
    private final SubmoduleRepository submoduleRepository;
    private final SeverityRepository severityRepository;
    private final DefectTypeRepository defectTypeRepository;
    private final TestCaseMapper testCaseMapper;
    private final DefectRepository defectRepository;
    private final ReleaseTestCaseRepository releaseTestCaseRepository;
    private final ReleaseViewRepository releaseViewRepository;


    // CREATE TEST CASE

    @Override
    public TestCaseResponseDTO createTestCase(
            TestCaseRequestDTO requestDTO) {

        if (requestDTO.getDescription() == null ||
                requestDTO.getDescription().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Description cannot be empty or contain only spaces"
            );
        }

        if (requestDTO.getTestSteps() == null ||
                requestDTO.getTestSteps().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Test steps cannot be empty or contain only spaces"
            );
        }

        // Allow letters, numbers, spaces and special characters
        // Example: 123, #@$%, abc123, Login @ 123!, etc.
        // Description must contain at least one letter
        if (!requestDTO.getDescription().matches(".*[a-zA-Z].*")) {

            throw new IllegalArgumentException(
                    "Description must contain at least one letter"
            );
        }

        // Test Steps must contain at least one letter
        if (!requestDTO.getTestSteps().matches(".*[a-zA-Z].*")) {

            throw new IllegalArgumentException(
                    "Test steps must contain at least one letter"
            );
        }

        ProjectDetails project = projectDetailsRepository
                .findById(requestDTO.getProjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with ID: "
                                        + requestDTO.getProjectId()
                        )
                );

        // Check project status
        if ("Completed".equalsIgnoreCase(project.getStatus())
                || "On Hold".equalsIgnoreCase(project.getStatus())) {

            throw new IllegalArgumentException(
                    "Cannot create test case for a completed or on-hold project."
            );
        }

        Module module = moduleRepositories
                .findById(requestDTO.getModuleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Module not found with ID: "
                                        + requestDTO.getModuleId()
                        )
                );

        SubModule subModule = submoduleRepository
                .findById(requestDTO.getSubModuleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sub Module not found with ID: "
                                        + requestDTO.getSubModuleId()
                        )
                );

        if (testCaseRepository.existsDuplicate(
                requestDTO.getSubModuleId(),
                requestDTO.getDescription().trim()
        )) {

            throw new DuplicateResourceException(
                    "Test case with this description already exists."
            );
        }

        Severity severity = severityRepository
                .findById(requestDTO.getSeverityId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Severity not found with ID: "
                                        + requestDTO.getSeverityId()
                        )
                );

        DefectType defectType = defectTypeRepository
                .findById(requestDTO.getDefectTypeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Defect Type not found with ID: "
                                        + requestDTO.getDefectTypeId()
                        )
                );

        TestCase testCase =
                testCaseMapper.toEntity(requestDTO);

        testCase.setProjectDetails(project);
        testCase.setModule(module);
        testCase.setSubModule(subModule);
        testCase.setSeverity(severity);
        testCase.setDefectType(defectType);

// Generate Test Case Number separately for each project
        Long maxTestCaseNumber =
                testCaseRepository.findMaxTestCaseNumberByProjectId(
                        project.getProjectId()
                ) ;

        if (maxTestCaseNumber == null) {
            maxTestCaseNumber = 0L;
        }

        Long nextTestCaseNumber =
                maxTestCaseNumber + 1;
        testCase.setTestCaseNumber(nextTestCaseNumber);



        TestCase savedTestCase =
                testCaseRepository.save(testCase);

        // Auto-allocate test case to existing releases for this project so it displays in Test Execution
        try {
            List<ReleaseView> releases = releaseViewRepository.findByProjectDetails_ProjectId(project.getProjectId());
            for (ReleaseView r : releases) {
                if (!releaseTestCaseRepository.existsByReleaseView_ReleaseIdAndTestCase_TestCaseId(r.getReleaseId(), savedTestCase.getTestCaseId())) {
                    ReleaseTestCase rtc = new ReleaseTestCase();
                    rtc.setReleaseView(r);
                    rtc.setTestCase(savedTestCase);
                    releaseTestCaseRepository.save(rtc);
                }
            }
        } catch (Exception ignored) {
        }

        return testCaseMapper.toResponseDTO(savedTestCase);
    }


    // FILTER TEST CASES

    @Override
    public List<TestCaseResponseDTO> filterTestCases(
            String description,
            Long defectTypeId,
            Long projectId,
            Long moduleId,
            Long subModuleId,
            Long severityId) {

        List<TestCase> testCases =
                testCaseRepository.filterTestCases(
                        description,
                        defectTypeId,
                        projectId,
                        moduleId,
                        subModuleId,
                        severityId
                );

        return testCases.stream()
                .map(testCaseMapper::toResponseDTO)
                .toList();
    }



    // GET TEST CASES BY PROJECT

    @Override
    public ResponseWrapper<Page<TestCaseResponseDTO>>
    getTestCasesByProject(
            Long projectId,
            Pageable pageable) {

        Page<TestCase> testCases =
                testCaseRepository.findByProjectIdOrderByTestCaseNumber(
                        projectId,
                        pageable
                );

        Page<TestCaseResponseDTO> responsePage =
                testCases.map(testCaseMapper::toResponseDTO);

        return new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS,
                responsePage,
                "Test cases retrieved successfully"
        );
    }


    // GET TEST CASES BY PROJECT + MODULE

    @Override
    public ResponseWrapper<Page<TestCaseResponseDTO>>
    getTestCasesByProjectAndModule(
            Long projectId,
            Long moduleId,
            Pageable pageable) {

        Page<TestCase> testCases =
                testCaseRepository.findByProjectIdAndModuleId(
                        projectId,
                        moduleId,
                        pageable
                );

        Page<TestCaseResponseDTO> responsePage =
                testCases.map(testCaseMapper::toResponseDTO);

        return new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS,
                responsePage,
                "Test cases retrieved successfully"
        );
    }



    // GET TEST CASES BY SUB MODULE


    @Override
    public ResponseWrapper<Page<TestCaseResponseDTO>>
    getTestCasesBySubModule(
            Long subModuleId,
            Pageable pageable) {

        Page<TestCase> testCases =
                testCaseRepository.findBySubModule_SubModuleIdOrderByTestCaseIdAsc(
                        subModuleId,
                        pageable
                );

        Page<TestCaseResponseDTO> responsePage =
                testCases.map(testCaseMapper::toResponseDTO);

        return new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS,
                responsePage,
                "Test cases retrieved successfully"
        );
    }


    // UPDATE TEST CASE


    // Update
    @Override
    @Transactional
    public TestCaseResponseDTO updateTestCase(
            Long testCaseId,
            TestCaseRequestDTO requestDTO) {

        // 1. Find existing test case
        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Test Case not found with ID: " + testCaseId
                        )
                );

        // 2. Validate description
        if (requestDTO.getDescription() == null
                || requestDTO.getDescription().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Description cannot be empty or contain only spaces"
            );
        }

        // 3. Validate test steps
        if (requestDTO.getTestSteps() == null
                || requestDTO.getTestSteps().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Test steps cannot be empty or contain only spaces"
            );
        }

        // 4. Get OLD values before modifying entity
        String oldDescription =
                testCase.getDescription() == null
                        ? ""
                        : testCase.getDescription().trim();

        String oldTestSteps =
                testCase.getTestSteps() == null
                        ? ""
                        : testCase.getTestSteps().trim();

        String oldTestCaseName =
                testCase.getTestCaseName() == null
                        ? ""
                        : testCase.getTestCaseName().trim();

        // 5. Get NEW values
        String newDescription =
                requestDTO.getDescription().trim();

        String newTestSteps =
                requestDTO.getTestSteps().trim();

        String newTestCaseName =
                requestDTO.getTestCaseName() == null
                        ? ""
                        : requestDTO.getTestCaseName().trim();

        // 4. Description validation
        if (requestDTO.getDescription() == null
                || requestDTO.getDescription().trim().isEmpty()) {


            throw new IllegalArgumentException(
                    "Description cannot be empty or contain only spaces"
            );
        }

        // 5. Test steps validation
        if (requestDTO.getTestSteps() == null
                || requestDTO.getTestSteps().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Test steps cannot be empty or contain only spaces"
            );
        }

        // 6. Description must contain at least one letter
        if (!newDescription.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException(
                    "Description must contain at least one letter"
            );
        }

        // 7. Test steps must contain at least one letter
        if (!newTestSteps.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException(
                    "Test steps must contain at least one letter"
            );
        }

        // 8. Check whether any actual change was made
        boolean descriptionChanged =
                !oldDescription.equals(newDescription);
        if (descriptionChanged) {

            if (testCaseRepository.existsDuplicateForUpdate(
                    testCase.getSubModule().getSubModuleId(),
                    newDescription,
                    testCaseId
            )) {

                throw new DuplicateResourceException(
                        "Test case with this description already exists."
                );
            }
        }
        boolean testStepsChanged =
                !oldTestSteps.equals(newTestSteps);

        boolean testCaseNameChanged =
                !oldTestCaseName.equals(newTestCaseName);

        boolean severityChanged =
                requestDTO.getSeverityId() != null
                        && (testCase.getSeverity() == null
                        || !testCase.getSeverity()
                        .getSeverityId()
                        .equals(requestDTO.getSeverityId()));

        boolean defectTypeChanged =
                requestDTO.getDefectTypeId() != null
                        && (testCase.getDefectType() == null
                        || !testCase.getDefectType()
                        .getDefectTypeId()
                        .equals(requestDTO.getDefectTypeId()));

        // 7. No changes
        if (!descriptionChanged
                && !testStepsChanged
                && !testCaseNameChanged
                && !severityChanged
                && !defectTypeChanged) {

            throw new IllegalArgumentException(
                    "No changes were made to the test case."
            );
        }

        // 10. Update basic fields
        testCase.setDescription(
                requestDTO.getDescription().trim()
        );

        if (requestDTO.getTestCaseName() != null) {
            testCase.setTestCaseName(
                    requestDTO.getTestCaseName().trim()
            );
        } else {
            testCase.setTestCaseName(null);
        }

        // 11. Update Severity
        if (requestDTO.getSeverityId() != null) {
            Severity severity = severityRepository
                    .findById(requestDTO.getSeverityId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Severity not found with ID: "
                                            + requestDTO.getSeverityId()
                            )
                    );

            testCase.setSeverity(severity);
        }

        // 12. Update Defect Type
        if (requestDTO.getDefectTypeId() != null) {

            DefectType defectType =
                    defectTypeRepository
                            .findById(requestDTO.getDefectTypeId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Defect Type not found with ID: "
                                                    + requestDTO.getDefectTypeId()
                                    )   );

            testCase.setDefectType(defectType);
        }


        // 13. Save only when there is a real change
        TestCase updatedTestCase =
                testCaseRepository.saveAndFlush(testCase);

        // 14. Update linked Defect with latest Test Case details
        List<Defect> linkedDefects =
                defectRepository.findAllByTestCase_TestCaseId(testCaseId);

        System.out.println("Test Case ID: " + testCaseId);
        System.out.println("Linked Defects Count: " + linkedDefects.size());

        for (Defect defect : linkedDefects) {

            defect.setBriefDescription(updatedTestCase.getDescription());
            defect.setSteps(updatedTestCase.getTestSteps());
            defect.setModule(updatedTestCase.getModule());
            defect.setSubModule(updatedTestCase.getSubModule());
            defect.setDefectType(updatedTestCase.getDefectType());
            defect.setSeverity(updatedTestCase.getSeverity());

            defectRepository.save(defect);
        }

        return testCaseMapper.toResponseDTO(updatedTestCase);
    }

    //delete
    @Override
    @Transactional
    public void deleteTestCase(Long testCaseId) {

        TestCase testCase =
                testCaseRepository.findById(testCaseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Test Case not found with ID: "
                                                + testCaseId
                                )
                        );

        boolean linked =
                defectRepository
                        .existsByTestCase_TestCaseId(testCaseId);

        if (linked) {

            throw new IllegalArgumentException(
                    "Cannot delete Test Case. "
                            + "This Test Case is already linked to a Defect."
            );
        }

        // Remove release allocations before deleting the test case
        releaseTestCaseRepository.deleteByTestCase_TestCaseId(testCaseId);

        testCaseRepository.delete(testCase);
    }



    // EXPORT TEST CASES TO EXCEL

    @Override
    public byte[] exportTestCases(
            Long projectId,
            Long moduleId,
            Long submoduleId) {

        List<TestCase> testCases;

        if (submoduleId != null) {

            testCases =
                    testCaseRepository
                            .findBySubModule_SubModuleIdOrderByTestCaseIdAsc(
                                    submoduleId
                            );

        } else if (moduleId != null) {

            testCases =
                    testCaseRepository
                            .findByProjectDetails_ProjectIdAndModule_ModuleIdOrderBySubModule_SubModuleIdAscTestCaseIdAsc(
                                    projectId,
                                    moduleId
                            );

        } else {

            testCases =
                    testCaseRepository
                            .findByProjectDetails_ProjectIdOrderByModule_ModuleIdAscSubModule_SubModuleIdAscTestCaseIdAsc(
                                    projectId
                            );
        }

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet("Test Cases");

            String[] headers = {
                    "Test Case ID",
                    "Test Case Name",
                    "Description",
                    "Test Steps",
                    "Project",
                    "Module",
                    "Sub Module",
                    "Severity",
                    "Defect Type"
            };

            Row headerRow =
                    sheet.createRow(0);

            CellStyle headerStyle =
                    workbook.createCellStyle();

            Font headerFont =
                    workbook.createFont();

            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0;
                 i < headers.length;
                 i++) {

                Cell cell =
                        headerRow.createCell(i);

                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNumber = 1;

            for (TestCase testCase : testCases) {

                Row row =
                        sheet.createRow(rowNumber++);

                row.createCell(0).setCellValue(
                        testCase.getTestCaseId() != null
                                ? testCase.getTestCaseId()
                                : 0
                );

                row.createCell(1).setCellValue(
                        testCase.getTestCaseName() != null
                                ? testCase.getTestCaseName()
                                : ""
                );

                row.createCell(2).setCellValue(
                        testCase.getDescription() != null
                                ? testCase.getDescription()
                                : ""
                );

                row.createCell(3).setCellValue(
                        testCase.getTestSteps() != null
                                ? testCase.getTestSteps()
                                : ""
                );

                row.createCell(4).setCellValue(
                        testCase.getProjectDetails() != null
                                ? testCase.getProjectDetails()
                                .getProjectName()
                                : ""
                );

                row.createCell(5).setCellValue(
                        testCase.getModule() != null
                                ? testCase.getModule()
                                .getModuleName()
                                : ""
                );

                row.createCell(6).setCellValue(
                        testCase.getSubModule() != null
                                ? testCase.getSubModule()
                                .getSubModuleName()
                                : ""
                );

                row.createCell(7).setCellValue(
                        testCase.getSeverity() != null
                                ? testCase.getSeverity()
                                .getSeverityName()
                                : ""
                );

                row.createCell(8).setCellValue(
                        testCase.getDefectType() != null
                                ? testCase.getDefectType()
                                .getDefectTypeName()
                                : ""
                );
            }

            for (int i = 0;
                 i < headers.length;
                 i++) {

                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to export test cases to Excel",
                    e
            );
        }
    }



    // IMPORT TEST CASES FROM EXCEL

    @Override
    @Transactional
    public void importTestCases(
            Long projectId,
            MultipartFile file) {


        // 1. Validate file

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please upload an Excel file."
            );
        }

        String fileName =
                file.getOriginalFilename();

        if (fileName == null ||
                (!fileName.toLowerCase().endsWith(".xlsx")
                        && !fileName.toLowerCase().endsWith(".xls"))) {

            throw new IllegalArgumentException(
                    "Only Excel files (.xlsx or .xls) are allowed."
            );
        }



        // 2. Find project

        ProjectDetails project =
                projectDetailsRepository
                        .findById(projectId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found with ID: "
                                                + projectId
                                )
                        );



        // 3. Check project status

        if ("Completed".equalsIgnoreCase(project.getStatus())
                || "On Hold".equalsIgnoreCase(project.getStatus())) {

            throw new IllegalArgumentException(
                    "Cannot import test cases for a completed or on-hold project."
            );
        }



        // 4. Read Excel

        try (
                InputStream inputStream =
                        file.getInputStream();

                Workbook workbook =
                        WorkbookFactory.create(inputStream)
        ) {

            if (workbook.getNumberOfSheets() == 0) {

                throw new IllegalArgumentException(
                        "Excel file does not contain any sheet."
                );
            }

            Sheet sheet =
                    workbook.getSheetAt(0);

            if (sheet == null) {

                throw new IllegalArgumentException(
                        "Excel file does not contain any sheet."
                );
            }



            // 5. Skip header row

            int rowCount = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);

                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                final int excelRow = i + 1;

                rowCount++;



                // 6. Read Excel columns

                String testCaseName =
                        getCellValue(row.getCell(1));

                String description =
                        getCellValue(row.getCell(2));

                String testSteps =
                        getCellValue(row.getCell(3));

                // Project column is intentionally ignored.
                // projectId comes from the API/frontend.

                String moduleName =
                        getCellValue(row.getCell(5));

                String subModuleName =
                        getCellValue(row.getCell(6));

                String severityName =
                        getCellValue(row.getCell(7));

                String defectTypeName =
                        getCellValue(row.getCell(8));


                // 7. Validate required fields

                if (description.isBlank()) {

                    throw new IllegalArgumentException(
                            "Description is empty at Excel row "
                                    + (i + 1)
                    );
                }

                if (testSteps.isBlank()) {

                    throw new IllegalArgumentException(
                            "Test Steps are empty at Excel row "
                                    + (i + 1)
                    );
                }

                if (moduleName.isBlank()) {

                    throw new IllegalArgumentException(
                            "Module is empty at Excel row "
                                    + (i + 1)
                    );
                }

                if (subModuleName.isBlank()) {

                    throw new IllegalArgumentException(
                            "Sub Module is empty at Excel row "
                                    + (i + 1)
                    );
                }

                if (severityName.isBlank()) {

                    throw new IllegalArgumentException(
                            "Severity is empty at Excel row "
                                    + (i + 1)
                    );
                }

                if (defectTypeName.isBlank()) {

                    throw new IllegalArgumentException(
                            "Defect Type is empty at Excel row "
                                    + (i + 1)
                    );
                }


                // 8. Find Module

                Module module =
                        moduleRepositories
                                .findByProject_ProjectIdAndModuleNameIgnoreCase(
                                        projectId,
                                        moduleName.trim()
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Module not found: "
                                                        + moduleName
                                                        + " at Excel row "
                                                        + excelRow
                                        )
                                );


                // 9. Find Sub Module

                SubModule subModule =
                        submoduleRepository
                                .findByModule_ModuleIdAndSubModuleNameIgnoreCase(
                                        module.getModuleId(),
                                        subModuleName.trim()
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Sub Module not found: "
                                                        + subModuleName
                                                        + " at Excel row "
                                                        + excelRow
                                        )
                                );



                // 10. Find Severity

                Severity severity =
                        severityRepository
                                .findBySeverityNameIgnoreCase(
                                        severityName.trim()
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Severity not found: "
                                                        + severityName
                                                        + " at Excel row "
                                                        + excelRow
                                        )
                                );



                // 11. Find Defect Type

                DefectType defectType =
                        defectTypeRepository
                                .findByDefectTypeNameIgnoreCase(
                                        defectTypeName.trim()
                                )
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Defect Type not found: "
                                                        + defectTypeName
                                                        + " at Excel row "
                                                        + excelRow
                                        )
                                );


                // 12. Check duplicate

                if (testCaseRepository.existsDuplicate(
                        subModule.getSubModuleId(),
                        description.trim()
                )) {

                    throw new DuplicateResourceException(
                            "Test case with description '"
                                    + description.trim()
                                    + "' already exists at Excel row "
                                    + (i + 1)
                    );
                }


                // 13. Create TestCase

                TestCase testCase =
                        new TestCase();

                if (!testCaseName.isBlank()) {

                    testCase.setTestCaseName(
                            testCaseName.trim()
                    );

                } else {

                    testCase.setTestCaseName(null);
                }

                testCase.setDescription(
                        description.trim()
                );

                testCase.setTestSteps(
                        testSteps.trim()
                );

                testCase.setProjectDetails(
                        project
                );

                testCase.setModule(
                        module
                );

                testCase.setSubModule(
                        subModule
                );

                testCase.setSeverity(
                        severity
                );

                testCase.setDefectType(
                        defectType
                );

                Long maxTestCaseNumber =
                        testCaseRepository.findMaxTestCaseNumberByProjectId(
                                projectId
                        );

                if (maxTestCaseNumber == null) {
                    maxTestCaseNumber = 0L;
                }

                testCase.setTestCaseNumber(++maxTestCaseNumber);

                testCaseRepository.save(testCase);
            }


            // 15. Check data

            if (rowCount == 0) {

                throw new IllegalArgumentException(
                        "Excel file does not contain any test cases."
                );
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to read Excel file.",
                    e
            );
        }
    }


    // EXCEL CELL VALUE

    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        DataFormatter formatter =
                new DataFormatter();

        return formatter
                .formatCellValue(cell)
                .trim();
    }


    // CHECK EMPTY ROW

    private boolean isRowEmpty(Row row) {

        if (row == null) {
            return true;
        }

        for (int i = 0; i <= 8; i++) {

            Cell cell =
                    row.getCell(i);

            if (cell != null &&
                    !getCellValue(cell).isEmpty()) {

                return false;
            }
        }

        return true;
    }
}