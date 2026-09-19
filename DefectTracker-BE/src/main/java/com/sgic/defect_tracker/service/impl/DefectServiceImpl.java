package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.DefectFilterDTO;
import com.sgic.defect_tracker.dtos.request.DefectRequestDTO;
import com.sgic.defect_tracker.dtos.response.DefectByModuleResponseDTO;
import com.sgic.defect_tracker.dtos.response.DefectDailyCountResponseDTO;
import com.sgic.defect_tracker.dtos.response.DefectResponseDTO;
import com.sgic.defect_tracker.entities.*;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.mapper.DefectMapper;
import com.sgic.defect_tracker.repositories.*;
import com.sgic.defect_tracker.service.AzureBlobStorageService;
import com.sgic.defect_tracker.service.DefectService;
import com.sgic.defect_tracker.service.EmailNotificationService;
import com.sgic.defect_tracker.specification.DefectSpecification;
import com.sgic.defect_tracker.utils.ValidationMessages;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import com.sgic.defect_tracker.dtos.response.ImportDefectResponseDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.*;
import java.util.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import com.sgic.defect_tracker.dtos.response.DefectSeverityBreakdownResponseDTO;
import com.sgic.defect_tracker.dtos.response.SeverityBreakdownItemDTO;
import com.sgic.defect_tracker.entities.Severity;
import com.sgic.defect_tracker.entities.StatusType;
//Exce/
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//Style
import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.*;
import java.util.ArrayList;
import com.sgic.defect_tracker.entities.ProjectDetails;
import com.sgic.defect_tracker.entities.Module;
import com.sgic.defect_tracker.entities.SubModule;
import com.sgic.defect_tracker.entities.*;

import static com.sgic.defect_tracker.utils.ValidationMessages.NOT_FOUND;

@Service
public class DefectServiceImpl implements DefectService {

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private DefectMapper defectMapper;

    @Autowired
    private DefectTypeRepository defectTypeRepository;

    @Autowired
    private ModuleRepositories moduleRepository;

    @Autowired
    private SubmoduleRepository subModuleRepository;

    @Autowired
    private SeverityRepository severityRepository;

    @Autowired
    private PriorityRepository priorityRepository;

    @Autowired
    private StatusTypeRepository statusTypeRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private com.sgic.defect_tracker.service.WorkFlowService workFlowService;

    @Autowired
    private ProjectDetailsRepository projectRepository;

    @Autowired
    private ReleaseViewRepository releaseViewRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DefectHistoryRepository defectHistoryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EmailNotificationService notificationService;

    @Autowired
    private TestCaseRepository testCaseRepository;
    @Autowired
    private ReleaseTestCaseRepository releaseTestCaseRepository;
    @Autowired
    private AzureBlobStorageService azureBlobStorageService;


    // =========================================================
    // CREATE DEFECT
    // =========================================================

    @Override
    @Transactional
    public DefectResponseDTO createdefect(
            DefectRequestDTO dto,
            MultipartFile attachmentImage) {

        System.out.println("attachmentImage = " + attachmentImage);

        if (attachmentImage != null) {
            System.out.println(
                    "File name = " + attachmentImage.getOriginalFilename()
            );
            System.out.println(
                    "File size = " + attachmentImage.getSize()
            );
            System.out.println(
                    "Content type = " + attachmentImage.getContentType()
            );
        }

        // =====================================================
        // NORMALIZE DESCRIPTION
        // =====================================================

        String description = dto.getBriefDescription()
                .trim()
                .replaceAll("\\s+", " ");

        // =====================================================
        // DUPLICATE DESCRIPTION CHECK
        // =====================================================

        if (defectRepository.existsByBriefDescriptionIgnoreCase(description)) {
            throw new IllegalArgumentException(
                    "Brief description already exists in another defect"
            );
        }

        dto.setBriefDescription(description);

        // =====================================================
        // MAP DTO TO ENTITY
        // =====================================================

        Defect defect = defectMapper.toEntity(dto);

        // =====================================================
        // SET ENTERED BY (CURRENT AUTHENTICATED EMPLOYEE)
        // =====================================================
        if (dto.getEnterBy() != null && !dto.getEnterBy().isBlank()) {
            defect.setEnterBy(dto.getEnterBy().trim());
        }
        if (defect.getEnterBy() == null || defect.getEnterBy().isBlank()) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                    String authEmail = authentication.getName();
                    if (authEmail != null && !authEmail.isBlank()) {
                        employeeRepository.findByEmailIgnoreCase(authEmail.trim()).ifPresent(emp -> {
                            String fullName = (emp.getFirstName() != null ? emp.getFirstName() : "") +
                                    (emp.getLastName() != null && !emp.getLastName().isBlank() ? " " + emp.getLastName() : "");
                            defect.setEnterBy(fullName.trim().isEmpty() ? authEmail : fullName.trim());
                        });
                    }
                }
            } catch (Exception ignored) {}
        }


        // =====================================================
        // SET MODULE
        // =====================================================

        if (dto.getModuleId() != null) {
            moduleRepository.findById(dto.getModuleId())
                    .ifPresent(defect::setModule);
        }

        // =====================================================
        // SET SUB MODULE
        // =====================================================

        if (dto.getSubModuleId() != null) {
            subModuleRepository.findById(dto.getSubModuleId())
                    .ifPresent(defect::setSubModule);
        }

        // =====================================================
        // SET DEFECT TYPE
        // =====================================================

        if (dto.getDefectTypeId() != null) {
            defectTypeRepository.findById(dto.getDefectTypeId())
                    .ifPresent(defect::setDefectType);
        }

        // =====================================================
        // SET SEVERITY
        // =====================================================

        if (dto.getSeverityId() != null) {
            severityRepository.findById(dto.getSeverityId())
                    .ifPresent(defect::setSeverity);
        } else {
            severityRepository.findAllByOrderByWeightAsc().stream()
                    .findFirst()
                    .ifPresent(defect::setSeverity);
        }

        // =====================================================
        // SET PRIORITY
        // =====================================================

        if (dto.getPriorityId() != null) {
            priorityRepository.findById(dto.getPriorityId())
                    .ifPresent(defect::setPriority);
        } else {
            priorityRepository.findAll().stream()
                    .findFirst()
                    .ifPresent(defect::setPriority);
        }

        // =====================================================
        // SET STATUS: Initial status must be the first status defined in the Workflow
        // =====================================================

        Long startStatusId = null;
        try {
            startStatusId = workFlowService.getStartingStatusTypeId();
        } catch (Exception ignored) {
        }

        if (startStatusId != null) {
            statusTypeRepository.findById(startStatusId).ifPresent(defect::setStatusType);
        } else if (dto.getStatusTypeId() != null) {
            statusTypeRepository.findById(dto.getStatusTypeId())
                    .ifPresent(defect::setStatusType);
        } else {
            statusTypeRepository.findByStatusNameIgnoreCase("New")
                    .or(() -> statusTypeRepository.findAll().stream().findFirst())
                    .ifPresent(defect::setStatusType);
        }

        // =====================================================
        // SET PROJECT
        // =====================================================

//        if (dto.getProjectId() != null) {
//            projectRepository.findById(dto.getProjectId())
//                    .ifPresent(defect::setProjectDetails);
//        }
        ProjectDetails project =
                projectRepository.findById(dto.getProjectId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                )
                        );

        defect.setProjectDetails(project);
        // =====================================================
// GENERATE PROJECT DEFECT NUMBER
// =====================================================
        Long maxNumber =
                defectRepository.findMaxProjectDefectNumber(
                        project.getProjectId()
                );

        Long nextNumber = maxNumber + 1;

        defect.setProjectDefectNumber(nextNumber);


        // =====================================================
        // SET ASSIGNEE
        // =====================================================

        if (dto.getAssignToId() != null) {
            employeeRepository.findById(dto.getAssignToId())
                    .ifPresent(defect::setAssignTo);
        }



        // =====================================================
        // SAVE ATTACHMENT IMAGE
        // =====================================================
        String imageUrl = null;



        if (attachmentImage != null && !attachmentImage.isEmpty()) {
            try {
                imageUrl = azureBlobStorageService.uploadImage(attachmentImage);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image to Azure Blob Storage", e);
            }
        }

        defect.setAttachmentImage(imageUrl);

        defect.setAttachmentImage(imageUrl);
//        if (attachmentImage != null && !attachmentImage.isEmpty()) {
//
//            try {
//
//                String uploadDir = "public/uploads/defects/";
//
//                Path uploadPath = Paths.get(uploadDir);
//
//                if (!Files.exists(uploadPath)) {
//                    Files.createDirectories(uploadPath);
//                }
//
//                String originalFileName =
//                        attachmentImage.getOriginalFilename();
//
//                String fileExtension = "";
//
//                if (originalFileName != null
//                        && originalFileName.contains(".")) {
//
//                    fileExtension =
//                            originalFileName.substring(
//                                    originalFileName.lastIndexOf(".")
//                            );
//                }
//
//                String fileName =
//                        UUID.randomUUID() + fileExtension;
//
//                Path filePath =
//                        uploadPath.resolve(fileName);
//
//                Files.copy(
//                        attachmentImage.getInputStream(),
//                        filePath,
//                        StandardCopyOption.REPLACE_EXISTING
//                );
//
//                defect.setAttachmentImage(
//                        "/uploads/defects/" + fileName
//                );
//
//            } catch (IOException e) {
//
//                throw new RuntimeException(
//                        "Failed to save attachment image",
//                        e
//                );
//            }
//        }

        // =====================================================
        // SET RELEASES
        // =====================================================

        if (dto.getReleaseIds() != null
                && !dto.getReleaseIds().isEmpty()) {

            List<ReleaseView> releases =
                    releaseViewRepository.findAllById(
                            dto.getReleaseIds()
                    );

            defect.setReleaseViews(releases);
        }

        // =====================================================
        // SET TEST CASE
        // =====================================================
        boolean isTestCaseRequired = Boolean.TRUE.equals(dto.getTestCaseRequired())
                || Boolean.TRUE.equals(dto.getIsAddTestCase());
        defect.setTestCaseRequired(isTestCaseRequired);

        if (dto.getTestCaseId() != null) {
            testCaseRepository.findById(dto.getTestCaseId())
                    .ifPresent(defect::setTestCase);
        } else if (isTestCaseRequired) {
            TestCase testCase = new TestCase();
            String briefDesc = defect.getBriefDescription() != null ? defect.getBriefDescription().trim() : "Defect Test Case";
            String stepsDesc = defect.getSteps() != null && !defect.getSteps().isBlank()
                    ? defect.getSteps().trim()
                    : "Steps to reproduce: " + briefDesc;

            String tcDesc = briefDesc;
            if (defect.getSubModule() != null && defect.getSubModule().getSubModuleId() != null) {
                String candidate = tcDesc;
                int suffix = 1;
                while (testCaseRepository.existsDuplicate(defect.getSubModule().getSubModuleId(), candidate)) {
                    candidate = tcDesc + " (" + suffix + ")";
                    suffix++;
                }
                tcDesc = candidate;
            }

            testCase.setDescription(tcDesc);
            testCase.setTestCaseName(briefDesc);
            testCase.setTestSteps(stepsDesc);
            testCase.setProjectDetails(project);
            testCase.setModule(defect.getModule());
            testCase.setSubModule(defect.getSubModule());
            testCase.setSeverity(defect.getSeverity());
            testCase.setPriority(defect.getPriority());
            testCase.setDefectType(defect.getDefectType());

            Long maxNum = testCaseRepository.findMaxTestCaseNumberByProjectId(project.getProjectId());
            testCase.setTestCaseNumber((maxNum == null ? 0L : maxNum) + 1);

            TestCase savedTestCase = testCaseRepository.save(testCase);
            defect.setTestCase(savedTestCase);

            // Auto-allocate test case to release(s)
            try {
                List<ReleaseView> rels = (defect.getReleaseViews() != null && !defect.getReleaseViews().isEmpty())
                        ? defect.getReleaseViews()
                        : releaseViewRepository.findByProjectDetails_ProjectId(project.getProjectId());
                if (rels != null) {
                    for (ReleaseView r : rels) {
                        if (!releaseTestCaseRepository.existsByReleaseView_ReleaseIdAndTestCase_TestCaseId(r.getReleaseId(), savedTestCase.getTestCaseId())) {
                            ReleaseTestCase rtc = new ReleaseTestCase();
                            rtc.setReleaseView(r);
                            rtc.setTestCase(savedTestCase);
                            releaseTestCaseRepository.save(rtc);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        } else {
            defect.setTestCase(null);
        }

        // =====================================================
        // SAVE DEFECT
        // =====================================================

        Defect saved = defectRepository.save(defect);

        // =====================================================
        // DEFECT CREATED HISTORY
        // =====================================================

        DefectHistory history = new DefectHistory();

        history.setDefect(saved);
        history.setAssignedTo(saved.getAssignTo());

        history.setPreviousStatus(null);

        history.setDefectStatus(
                saved.getStatusType() != null
                        ? saved.getStatusType().getStatusName()
                        : null
        );

        if (saved.getReleaseViews() != null && !saved.getReleaseViews().isEmpty()) {
            history.setReleaseName(saved.getReleaseViews().getFirst().getReleaseName());
        }
        history.setName("Defect Created");

        history.setDefectDate(LocalDate.now());
        history.setDefectTime(LocalTime.now());

        defectHistoryRepository.save(history);

        // =====================================================
        // DEFECT ASSIGNED EMAIL
        // Only send when defect is created with an assignee
        // =====================================================

        if (saved.getAssignTo() != null) {

            Employee assignedEmployee = saved.getAssignTo();

            Map<String, Object> variables = Map.of(
                    "employeeName",
                    assignedEmployee.getFirstName()
                            + " "
                            + assignedEmployee.getLastName(),

                    "projectName",
                    saved.getProjectDetails() != null
                            ? saved.getProjectDetails().getProjectName()
                            : "",

                    "defectId",
                    saved.getDefectId(),

                    "briefDescription",
                    saved.getBriefDescription() != null
                            ? saved.getBriefDescription()
                            : "",

                    "statusType",
                    saved.getStatusType() != null && saved.getStatusType().getStatusType() != null
                            ? saved.getStatusType().getStatusType()
                            : ""
            );

            notificationService.sendEmail(
                    "DEFECT_ASSIGNED",
                    assignedEmployee.getEmail(),
                    variables
            );
        }

        return defectMapper.toResponse(saved);
    }
    @Override
    @Transactional
    public void bulkReassignDefects(
            List<Long> defectIds,
            Long assignedToId) {

        if (defectIds == null || defectIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select at least one defect"
            );
        }

        if (assignedToId == null) {
            throw new IllegalArgumentException(
                    "Please select a developer"
            );
        }

        Employee assignedEmployee =
                employeeRepository.findById(assignedToId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee not found"
                                )
                        );

        List<Defect> defects =
                defectRepository.findAllById(defectIds);

        if (defects.size() != defectIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more defects not found"
            );
        }

        for (Defect defect : defects) {

            Employee previousAssignedEmployee =
                    defect.getAssignTo();

            // Update developer
            defect.setAssignTo(assignedEmployee);

            Defect updatedDefect =
                    defectRepository.save(defect);

            // ============================
            // SAVE DEFECT HISTORY
            // ============================

            DefectHistory history =
                    new DefectHistory();

            history.setDefect(updatedDefect);
            history.setAssignedTo(assignedEmployee);

            history.setPreviousStatus(
                    updatedDefect.getStatusType() != null
                            ? updatedDefect
                            .getStatusType()
                            .getStatusType()
                            : null
            );

            history.setDefectStatus(
                    updatedDefect.getStatusType() != null
                            ? updatedDefect
                            .getStatusType()
                            .getStatusType()
                            : null
            );

            history.setName("Assignee Changed");
            history.setDefectDate(LocalDate.now());
            history.setDefectTime(LocalTime.now());

            defectHistoryRepository.save(history);

            // ============================
            // SEND REASSIGN EMAIL
            // ============================

            boolean assignmentChanged =
                    previousAssignedEmployee == null
                            || !previousAssignedEmployee
                            .getEmpId()
                            .equals(
                                    assignedEmployee.getEmpId()
                            );

            if (assignmentChanged) {

                Map<String, Object> variables =
                        Map.of(
                                "employeeName",
                                assignedEmployee.getFirstName()
                                        + " "
                                        + assignedEmployee.getLastName(),

                                "projectName",
                                updatedDefect.getProjectDetails() != null
                                        ? updatedDefect
                                        .getProjectDetails()
                                        .getProjectName()
                                        : "",

                                "defectId",
                                updatedDefect.getDefectId(),

                                "briefDescription",
                                updatedDefect.getBriefDescription() != null
                                        ? updatedDefect.getBriefDescription()
                                        : "",

                                "statusType",
                                updatedDefect.getStatusType() != null && updatedDefect.getStatusType().getStatusType() != null
                                        ? updatedDefect
                                        .getStatusType()
                                        .getStatusType()
                                        : "",

                                "assignTo",
                                previousAssignedEmployee != null
                                        ? previousAssignedEmployee
                                        .getFirstName()
                                        + " "
                                        + previousAssignedEmployee
                                        .getLastName()
                                        : "",

                                "firstName",
                                assignedEmployee.getFirstName(),

                                "lastName",
                                assignedEmployee.getLastName()
                        );

                notificationService.sendEmail(
                        "DEFECT_REASSIGNED",
                        assignedEmployee.getEmail(),
                        variables
                );
            }
        }
    }

    @Override
    @Transactional
    public byte[] exportDefectsToExcel(Long projectId) {

        if (projectId == null) {
            throw new IllegalArgumentException(
                    "Project ID is required"
            );
        }

        projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Project not found with ID: " + projectId
                        )
                );

        List<Defect> defects = defectRepository
                .findAll()
                .stream()
                .filter(defect ->
                        defect.getProjectDetails() != null
                                && projectId.equals(
                                defect.getProjectDetails().getProjectId()
                        )
                )
                .toList();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Defects");

            String[] headers = {
                    "DEFECT ID",
                    "BRIEF DESCRIPTION",
                    "STEPS",
                    "MODULE",
                    "SUBMODULE",
                    "TYPE",
                    "SEVERITY",
                    "PRIORITY",
                    "STATUS",
                    "ASSIGNED TO",
                    "ENTERED BY",
                    "RELEASE"
            };


            CellStyle headerStyle = workbook.createCellStyle();

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {

                Cell cell = headerRow.createCell(i);

                cell.setCellValue(headers[i]);

                cell.setCellStyle(headerStyle);
            }



            int rowIndex = 1;

            for (Defect defect : defects) {

                Row row = sheet.createRow(rowIndex++);

                // DEFECT ID
                row.createCell(0).setCellValue(
                        defect.getDefectId() != null
                                ? defect.getDefectId()
                                : 0
                );

                // BRIEF DESCRIPTION
                row.createCell(1).setCellValue(
                        defect.getBriefDescription() != null
                                ? defect.getBriefDescription()
                                : ""
                );

                // STEPS
                row.createCell(2).setCellValue(
                        defect.getSteps() != null
                                ? defect.getSteps()
                                : ""
                );

                // MODULE
                row.createCell(3).setCellValue(
                        defect.getModule() != null
                                && defect.getModule().getModuleName() != null
                                ? defect.getModule().getModuleName()
                                : ""
                );

                // SUBMODULE
                row.createCell(4).setCellValue(
                        defect.getSubModule() != null
                                && defect.getSubModule().getSubModuleName() != null
                                ? defect.getSubModule().getSubModuleName()
                                : ""
                );

                // TYPE
                row.createCell(5).setCellValue(
                        defect.getDefectType() != null
                                && defect.getDefectType().getDefectTypeName() != null
                                ? defect.getDefectType().getDefectTypeName()
                                : ""
                );

                // SEVERITY
                row.createCell(6).setCellValue(
                        defect.getSeverity() != null
                                && defect.getSeverity().getSeverityName() != null
                                ? defect.getSeverity().getSeverityName()
                                : ""
                );

                // PRIORITY
                row.createCell(7).setCellValue(
                        defect.getPriority() != null
                                && defect.getPriority().getPriorityName() != null
                                ? defect.getPriority().getPriorityName()
                                : ""
                );

                // STATUS
                row.createCell(8).setCellValue(
                        defect.getStatusType() != null
                                && defect.getStatusType().getStatusName() != null
                                ? defect.getStatusType().getStatusName()
                                : ""
                );

                // ASSIGNED TO
                String assignedTo = "";

                if (defect.getAssignTo() != null) {

                    String firstName =
                            defect.getAssignTo().getFirstName();

                    String lastName =
                            defect.getAssignTo().getLastName();

                    if (firstName != null) {
                        assignedTo = firstName;
                    }

                    if (lastName != null
                            && !lastName.isBlank()) {

                        if (!assignedTo.isBlank()) {
                            assignedTo += " ";
                        }

                        assignedTo += lastName;
                    }
                }

                row.createCell(9).setCellValue(
                        assignedTo
                );

                // ENTERED BY
                row.createCell(10).setCellValue(
                        defect.getEnterBy() != null
                                ? defect.getEnterBy()
                                : ""
                );

                // RELEASE
                StringBuilder releaseText =
                        new StringBuilder();

                if (defect.getReleaseViews() != null) {

                    for (ReleaseView releaseView :
                            defect.getReleaseViews()) {

                        if (releaseView.getReleaseName() != null
                                && !releaseView.getReleaseName().isBlank()) {

                            if (releaseText.length() > 0) {
                                releaseText.append(" | ");
                            }

                            releaseText.append(
                                    releaseView.getReleaseName()
                            );
                        }
                    }
                }

                row.createCell(11).setCellValue(
                        releaseText.toString()
                );
            }


            for (int i = 0; i < headers.length; i++) {

                sheet.autoSizeColumn(i);

                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to export defects to Excel",
                    e
            );
        }
    }


    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        DataFormatter formatter = new DataFormatter();

        return formatter.formatCellValue(cell).trim();
    }

    @Override
    @Transactional
    public ImportDefectResponseDTO importDefects(
            MultipartFile file,
            Long projectId
    ) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload an Excel or CSV file"
            );
        }

        if (projectId == null) {
            throw new IllegalArgumentException(
                    "Project ID is required"
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new IllegalArgumentException(
                    "File name is missing"
            );
        }

        String lowerFileName = fileName.toLowerCase();

        if (!lowerFileName.endsWith(".xlsx")) {
            throw new IllegalArgumentException(
                    "Please upload an Excel (.xlsx) file"
            );
        }


        ProjectDetails project = projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Project not found with ID: " + projectId
                        )
                );


        List<String> expectedHeaders = List.of(
                "DEFECT ID",
                "BRIEF DESCRIPTION",
                "STEPS",
                "MODULE",
                "SUBMODULE",
                "TYPE",
                "SEVERITY",
                "PRIORITY",
                "STATUS",
                "ASSIGNED TO",
                "ENTERED BY",
                "RELEASE"
        );

        ImportDefectResponseDTO response =
                new ImportDefectResponseDTO();

        int total = 0;
        int imported = 0;
        int failed = 0;
        int skipped = 0;

        try (Workbook workbook =
                     WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException(
                        "Excel file is empty"
                );
            }


            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException(
                        "Excel header row is missing"
                );
            }

            for (int i = 0; i < expectedHeaders.size(); i++) {

                String actualHeader =
                        getCellValue(headerRow.getCell(i));

                String expectedHeader =
                        expectedHeaders.get(i);

                if (!expectedHeader.equalsIgnoreCase(actualHeader)) {

                    throw new IllegalArgumentException(
                            "Invalid header at column "
                                    + (i + 1)
                                    + ". Expected: "
                                    + expectedHeader
                                    + ", Found: "
                                    + actualHeader
                    );
                }
            }

            System.out.println(
                    "Excel headers validated successfully."
            );


            for (int rowIndex = 1;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    continue;
                }

                boolean emptyRow = true;

                for (int cellIndex = 0;
                     cellIndex < expectedHeaders.size();
                     cellIndex++) {

                    if (!getCellValue(
                            row.getCell(cellIndex)
                    ).isBlank()) {

                        emptyRow = false;
                        break;
                    }
                }

                if (emptyRow) {
                    continue;
                }

                total++;

                try {


                    String defectId =
                            getCellValue(row.getCell(0));

                    String briefDescription =
                            getCellValue(row.getCell(1));

                    String steps =
                            getCellValue(row.getCell(2));

                    String moduleName =
                            getCellValue(row.getCell(3));

                    String subModuleName =
                            getCellValue(row.getCell(4));

                    String defectTypeName =
                            getCellValue(row.getCell(5));

                    String severityName =
                            getCellValue(row.getCell(6));

                    String priorityName =
                            getCellValue(row.getCell(7));

                    String statusName =
                            getCellValue(row.getCell(8));

                    String assignedToName =
                            getCellValue(row.getCell(9));

                    String enteredBy =
                            getCellValue(row.getCell(10));

                    String releaseName =
                            getCellValue(row.getCell(11));


                    if (briefDescription.isBlank()) {

                        failed++;

                        System.out.println(
                                "Row " + (rowIndex + 1) +
                                        " failed: Brief Description is required"
                        );

                        continue;
                    }

                    if (steps.isBlank()) {

                        failed++;

                        System.out.println(
                                "Row " + (rowIndex + 1) +
                                        " failed: Steps is required"
                        );

                        continue;
                    }


                    Module module = null;

                    if (!moduleName.isBlank()) {

                        module = moduleRepository
                                .findByProject_ProjectIdAndModuleNameContainingIgnoreCase(
                                        projectId,
                                        moduleName
                                )
                                .stream()
                                .findFirst()
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Module not found: "
                                                        + moduleName
                                        )
                                );
                    }

                    SubModule subModule = null;

                    if (!subModuleName.isBlank()) {

                        if (module == null) {

                            throw new IllegalArgumentException(
                                    "Module is required when SubModule is provided"
                            );
                        }

                        subModule = subModuleRepository
                                .findBySubModuleNameIgnoreCaseAndModule_ModuleId(
                                        subModuleName,
                                        module.getModuleId()
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "SubModule not found: "
                                                        + subModuleName
                                        )
                                );
                    }

                    if (module == null) {

                        throw new IllegalArgumentException(
                                "Module is required for defect import"
                        );
                    }

                    if (subModule == null) {

                        throw new IllegalArgumentException(
                                "SubModule is required for defect import"
                        );
                    }

                    boolean duplicate =
                            defectRepository
                                    .existsByProjectDetails_ProjectIdAndModule_ModuleIdAndSubModule_SubModuleIdAndBriefDescriptionIgnoreCaseAndSteps(
                                            projectId,
                                            module.getModuleId(),
                                            subModule.getSubModuleId(),
                                            briefDescription.trim(),
                                            steps.trim()
                                    );

                    if (duplicate) {

                        skipped++;

                        System.out.println(
                                "Row " + (rowIndex + 1) +
                                        " skipped: Duplicate defect already exists"
                        );

                        continue;
                    }

                    DefectType defectType = null;

                    if (!defectTypeName.isBlank()) {

                        defectType = defectTypeRepository
                                .findAll()
                                .stream()
                                .filter(type ->
                                        type.getDefectTypeName() != null
                                                && type.getDefectTypeName()
                                                .equalsIgnoreCase(
                                                        defectTypeName
                                                )
                                )
                                .findFirst()
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Defect Type not found: "
                                                        + defectTypeName
                                        )
                                );
                    }

                    Severity severity = null;

                    if (!severityName.isBlank()) {

                        severity = severityRepository
                                .findAll()
                                .stream()
                                .filter(s ->
                                        s.getSeverityName() != null
                                                && s.getSeverityName()
                                                .equalsIgnoreCase(
                                                        severityName
                                                )
                                )
                                .findFirst()
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Severity not found: "
                                                        + severityName
                                        )
                                );
                    }


                    Priority priority = null;

                    if (!priorityName.isBlank()) {

                        priority = priorityRepository
                                .findByPriorityNameIgnoreCase(
                                        priorityName
                                );

                        if (priority == null) {

                            throw new IllegalArgumentException(
                                    "Priority not found: "
                                            + priorityName
                            );
                        }
                    }


                    StatusType statusType = null;

                    if (!statusName.isBlank()) {

                        statusType = statusTypeRepository
                                .findAll()
                                .stream()
                                .filter(status ->
                                        status.getStatusName() != null
                                                && status.getStatusName()
                                                .equalsIgnoreCase(
                                                        statusName
                                                )
                                )
                                .findFirst()
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Status not found: "
                                                        + statusName
                                        )
                                );
                    }


                    Employee employee = null;

                    if (!assignedToName.isBlank()) {

                        employee = employeeRepository
                                .findAll()
                                .stream()
                                .filter(emp -> {

                                    String fullName =
                                            ((emp.getFirstName() != null)
                                                    ? emp.getFirstName()
                                                    : "")
                                                    + " "
                                                    + ((emp.getLastName() != null)
                                                    ? emp.getLastName()
                                                    : "");

                                    return fullName.trim()
                                            .equalsIgnoreCase(
                                                    assignedToName.trim()
                                            );
                                })
                                .findFirst()
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Employee not found: "
                                                        + assignedToName
                                        )
                                );
                    }


                    ReleaseView release = null;

                    if (!releaseName.isBlank()) {

                        release = releaseViewRepository
                                .findByProjectDetails_ProjectId(projectId)
                                .stream()
                                .filter(r ->
                                        r.getReleaseName() != null
                                                && r.getReleaseName()
                                                .equalsIgnoreCase(
                                                        releaseName
                                                )
                                )
                                .findFirst()
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Release not found: "
                                                        + releaseName
                                        )
                                );
                    }


                    Defect defect = new Defect();

                    defect.setBriefDescription(
                            briefDescription.trim()
                    );

                    defect.setSteps(
                            steps.trim()
                    );


                    defect.setTestCaseRequired(false);

                    if (enteredBy == null || enteredBy.isBlank()) {
                        defect.setEnterBy(null);
                    } else {
                        defect.setEnterBy(
                                enteredBy.trim()
                        );
                    }

                    defect.setModule(module);

                    defect.setSubModule(subModule);

                    defect.setDefectType(defectType);

                    defect.setSeverity(severity);

                    defect.setPriority(priority);

                    defect.setStatusType(statusType);

                    defect.setProjectDetails(project);

                    defect.setAssignTo(employee);


                    if (release != null) {

                        defect.setReleaseViews(
                                new ArrayList<>(
                                        List.of(release)
                                )
                        );
                    }


                    defectRepository.save(defect);

                    imported++;

                    System.out.println(
                            "Row " + (rowIndex + 1) +
                                    " imported successfully"
                    );

                } catch (Exception e) {

                    failed++;

                    System.out.println(
                            "Row " + (rowIndex + 1) +
                                    " failed: " +
                                    e.getMessage()
                    );
                }
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to read Excel file",
                    e
            );
        }


        response.setTotal(total);
        response.setImported(imported);
        response.setFailed(failed);
        response.setSkipped(skipped);

        return response;
    }


    @Override
    @Transactional
    public DefectResponseDTO updatedefect(
            Long id,
            DefectRequestDTO dto,
            MultipartFile attachmentImage) {


        String description = dto.getBriefDescription()
                .trim()
                .replaceAll("\\s+", " ");


        Defect defect = defectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(NOT_FOUND)
                );

        if (defectRepository
                .existsByBriefDescriptionIgnoreCaseAndDefectIdNot(
                        description,
                        id)) {

            throw new IllegalArgumentException(
                    "Brief description already exists"
            );
        }


        String previousDescription =
                defect.getBriefDescription();

        String previousSteps =
                defect.getSteps();

        Long previousModuleId =
                defect.getModule() != null
                        ? defect.getModule().getModuleId()
                        : null;

        Long previousSubModuleId =
                defect.getSubModule() != null
                        ? defect.getSubModule().getSubModuleId()
                        : null;

        Long previousDefectTypeId =
                defect.getDefectType() != null
                        ? defect.getDefectType().getDefectTypeId()
                        : null;

        Long previousSeverityId =
                defect.getSeverity() != null
                        ? defect.getSeverity().getSeverityId()
                        : null;

        Long previousPriorityId =
                defect.getPriority() != null
                        ? defect.getPriority().getPriorityId()
                        : null;

        Long previousStatusId =
                defect.getStatusType() != null
                        ? defect.getStatusType().getStatusTypeId()
                        : null;

        Long previousProjectId =
                defect.getProjectDetails() != null
                        ? defect.getProjectDetails().getProjectId()
                        : null;

        Long previousAssignedToId =
                defect.getAssignTo() != null
                        ? defect.getAssignTo().getEmpId()
                        : null;


        String previousStatus =
                defect.getStatusType() != null
                        ? defect.getStatusType().getStatusType()
                        : null;

        Employee previousAssignedEmployee =
                defect.getAssignTo();

        String previousImage = defect.getAttachmentImage();

        boolean changed =
                !Objects.equals(
                        previousDescription,
                        description
                )
                        || !Objects.equals(
                        previousSteps,
                        dto.getSteps()
                )
                        || !Objects.equals(
                        previousModuleId,
                        dto.getModuleId()
                )
                        || !Objects.equals(
                        previousSubModuleId,
                        dto.getSubModuleId()
                )
                        || !Objects.equals(
                        previousDefectTypeId,
                        dto.getDefectTypeId()
                )
                        || !Objects.equals(
                        previousSeverityId,
                        dto.getSeverityId()
                )
                        || !Objects.equals(
                        previousPriorityId,
                        dto.getPriorityId()
                )
                        || !Objects.equals(
                        previousStatusId,
                        dto.getStatusTypeId()
                )
                        || !Objects.equals(
                        previousProjectId,
                        dto.getProjectId()
                )
                        || !Objects.equals(
                        previousAssignedToId,
                        dto.getAssignToId()
                )

                || (Boolean.TRUE.equals(dto.getRemoveAttachment())
                        && previousImage != null)

                || (attachmentImage != null && !attachmentImage.isEmpty())
                ;

        System.out.println("===== CHANGE CHECK =====");

        System.out.println(
                "Description: "
                        + previousDescription
                        + " -> "
                        + description
        );

        System.out.println(
                "Steps: "
                        + previousSteps
                        + " -> "
                        + dto.getSteps()
        );

        System.out.println(
                "Module: "
                        + previousModuleId
                        + " -> "
                        + dto.getModuleId()
        );

        System.out.println(
                "SubModule: "
                        + previousSubModuleId
                        + " -> "
                        + dto.getSubModuleId()
        );

        System.out.println(
                "DefectType: "
                        + previousDefectTypeId
                        + " -> "
                        + dto.getDefectTypeId()
        );

        System.out.println(
                "Severity: "
                        + previousSeverityId
                        + " -> "
                        + dto.getSeverityId()
        );

        System.out.println(
                "Priority: "
                        + previousPriorityId
                        + " -> "
                        + dto.getPriorityId()
        );

        System.out.println(
                "Status: "
                        + previousStatusId
                        + " -> "
                        + dto.getStatusTypeId()
        );

        System.out.println(
                "Project: "
                        + previousProjectId
                        + " -> "
                        + dto.getProjectId()
        );

        System.out.println(
                "AssignTo: "
                        + previousAssignedToId
                        + " -> "
                        + dto.getAssignToId()
        );

        System.out.println("Changed = " + changed);


        if (!changed) {

            throw new IllegalArgumentException(
                    "No changes detected. Please change at least one field."
            );
        }

        if (Boolean.TRUE.equals(dto.getRemoveAttachment())) {

            if (defect.getAttachmentImage() != null &&
                    !defect.getAttachmentImage().isEmpty()) {

                Path oldFile = Paths.get("public/uploads/defects/")
                        .resolve(Paths.get(defect.getAttachmentImage()).getFileName());

                try {
                    Files.deleteIfExists(oldFile);
                } catch (IOException e) {
                    throw new RuntimeException(
                            "Failed to delete old image",
                            e
                    );
                }
            }

            defect.setAttachmentImage(null);
        }


        else if (attachmentImage != null &&
                !attachmentImage.isEmpty()) {

            // Delete old image
            if (defect.getAttachmentImage() != null &&
                    !defect.getAttachmentImage().isEmpty()) {

                Path oldFile = Paths.get("public/uploads/defects/")
                        .resolve(Paths.get(defect.getAttachmentImage()).getFileName());

                try {
                    Files.deleteIfExists(oldFile);
                } catch (IOException e) {
                    throw new RuntimeException(
                            "Failed to delete old image",
                            e
                    );
                }
            }


            Path uploadPath =
                    Paths.get("public/uploads/defects/");

            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName =
                        UUID.randomUUID() + "_" +
                                attachmentImage.getOriginalFilename();

                Path filePath =
                        uploadPath.resolve(fileName);


                Files.copy(
                        attachmentImage.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
                defect.setAttachmentImage("/uploads/defects/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to save image",
                        e
                );
            }
        }


        dto.setBriefDescription(description);

        defectMapper.updatedefctfromdto(dto, defect);

        if (dto.getModuleId() != null) {

            defect.setModule(
                    moduleRepository.findById(
                                    dto.getModuleId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Module not found"
                                    )
                            )
            );
        }


        if (dto.getSubModuleId() != null) {

            defect.setSubModule(
                    subModuleRepository.findById(
                                    dto.getSubModuleId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "SubModule not found"
                                    )
                            )
            );
        }


        if (dto.getDefectTypeId() != null) {

            defect.setDefectType(
                    defectTypeRepository.findById(
                                    dto.getDefectTypeId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Defect Type not found"
                                    )
                            )
            );
        }


        if (dto.getSeverityId() != null) {

            defect.setSeverity(
                    severityRepository.findById(
                                    dto.getSeverityId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Severity not found"
                                    )
                            )
            );
        }


        if (dto.getPriorityId() != null) {

            defect.setPriority(
                    priorityRepository.findById(
                                    dto.getPriorityId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Priority not found"
                                    )
                            )
            );
        }


        if (dto.getStatusTypeId() != null) {
            Long newStatusId = dto.getStatusTypeId();
            Long currentStatusId = (defect.getStatusType() != null)
                    ? defect.getStatusType().getStatusTypeId()
                    : null;

            if (currentStatusId != null && !currentStatusId.equals(newStatusId)) {
                List<WorkFlow> allWorkflows = workflowRepository.findAll();
                if (allWorkflows != null && !allWorkflows.isEmpty()) {
                    boolean isAllowed = workflowRepository.existsByStatusTypeId1AndStatusTypeId2(currentStatusId, newStatusId);
                    if (!isAllowed) {
                        StatusType currentStatus = defect.getStatusType();
                        StatusType targetStatus = statusTypeRepository.findById(newStatusId).orElse(null);
                        String currentName = currentStatus != null ? currentStatus.getStatusName() : String.valueOf(currentStatusId);
                        String targetName = targetStatus != null ? targetStatus.getStatusName() : String.valueOf(newStatusId);
                        throw new IllegalArgumentException(
                                "Invalid status transition from " + currentName + " to " + targetName + " according to the configured workflow."
                        );
                    }
                }
            }

            defect.setStatusType(
                    statusTypeRepository.findById(
                                    newStatusId
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Status Type not found"
                                    )
                            )
            );
        }

        if (dto.getProjectId() != null) {

            defect.setProjectDetails(
                    projectRepository.findById(
                                    dto.getProjectId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Project not found"
                                    )
                            )
            );
        }

        if (dto.getAssignToId() != null) {

            defect.setAssignTo(
                    employeeRepository.findById(
                                    dto.getAssignToId()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Employee not found"
                                    )
                            )
            );
        }

        Defect updatedDefect =
                defectRepository.save(defect);

        String currentStatus =
                updatedDefect.getStatusType() != null
                        ? updatedDefect.getStatusType().getStatusType()
                        : null;

        Employee currentAssignedEmployee =
                updatedDefect.getAssignTo();


        String historyName = "Defect Updated";

        if (!Objects.equals(
                previousStatusId,
                dto.getStatusTypeId())) {

            historyName = "Status Changed";

        } else if (!Objects.equals(
                previousAssignedToId,
                dto.getAssignToId())) {

            historyName = "Assignee Changed";
        }


        DefectHistory history =
                new DefectHistory();

        history.setDefect(updatedDefect);

        history.setAssignedTo(
                currentAssignedEmployee
        );

        history.setPreviousStatus(previousStatus);

        history.setDefectStatus(currentStatus);

        history.setName(historyName);

        history.setDefectDate(LocalDate.now());

        history.setDefectTime(LocalTime.now());

        defectHistoryRepository.save(history);


        if (dto.getAssignToId() != null
                && currentAssignedEmployee != null) {

            boolean assignmentChanged =
                    previousAssignedEmployee == null
                            || !previousAssignedEmployee
                            .getEmpId()
                            .equals(currentAssignedEmployee.getEmpId());

            if (assignmentChanged) {

                if (previousAssignedEmployee == null) {
                    // First time assignment -> Send DEFECT_ASSIGNED
                    Map<String, Object> variables = Map.of(
                            "employeeName",
                            currentAssignedEmployee.getFirstName()
                                    + " "
                                    + currentAssignedEmployee.getLastName(),

                            "projectName",
                            updatedDefect.getProjectDetails() != null
                                    ? updatedDefect
                                    .getProjectDetails()
                                    .getProjectName()
                                    : "",

                            "defectId",
                            updatedDefect.getDefectId(),

                            "briefDescription",
                            updatedDefect.getBriefDescription() != null
                                    ? updatedDefect.getBriefDescription()
                                    : "",

                            "statusType",
                            updatedDefect.getStatusType() != null && updatedDefect.getStatusType().getStatusType() != null
                                    ? updatedDefect
                                    .getStatusType()
                                    .getStatusType()
                                    : ""
                    );

                    notificationService.sendEmail(
                            "DEFECT_ASSIGNED",
                            currentAssignedEmployee.getEmail(),
                            variables
                    );
                } else {
                    // Reassignment -> Send DEFECT_REASSIGNED
                    Map<String, Object> variables = Map.of(
                            "employeeName",
                            currentAssignedEmployee.getFirstName()
                                    + " "
                                    + currentAssignedEmployee.getLastName(),

                            "projectName",
                            updatedDefect.getProjectDetails() != null
                                    ? updatedDefect
                                    .getProjectDetails()
                                    .getProjectName()
                                    : "",

                            "defectId",
                            updatedDefect.getDefectId(),

                            "briefDescription",
                            updatedDefect.getBriefDescription() != null
                                    ? updatedDefect.getBriefDescription()
                                    : "",

                            "statusType",
                            updatedDefect.getStatusType() != null && updatedDefect.getStatusType().getStatusType() != null
                                    ? updatedDefect
                                    .getStatusType()
                                    .getStatusType()
                                    : "",

                            "assignTo",
                            previousAssignedEmployee.getFirstName()
                                    + " "
                                    + previousAssignedEmployee.getLastName(),

                            "firstName",
                            currentAssignedEmployee.getFirstName(),

                            "lastName",
                            currentAssignedEmployee.getLastName()
                    );

                    notificationService.sendEmail(
                            "DEFECT_REASSIGNED",
                            currentAssignedEmployee.getEmail(),
                            variables
                    );
                }
            }
        }

        if (dto.getStatusTypeId() != null
                && currentAssignedEmployee != null
                && updatedDefect.getStatusType() != null) {

            String newStatus =
                    updatedDefect
                            .getStatusType()
                            .getStatusType();

            boolean statusChanged =
                    !Objects.equals(
                            previousStatusId,
                            dto.getStatusTypeId()
                    );

            if (statusChanged) {

                Map<String, Object> variables =
                        Map.of(
                                "employeeName",
                                currentAssignedEmployee
                                        .getFirstName()
                                        + " "
                                        + currentAssignedEmployee
                                        .getLastName(),

                                "projectName",
                                updatedDefect
                                        .getProjectDetails() != null
                                        ? updatedDefect
                                        .getProjectDetails()
                                        .getProjectName()
                                        : "",

                                "defectId",
                                updatedDefect
                                        .getDefectId(),

                                "fromStatus",
                                previousStatus != null
                                        ? previousStatus
                                        : "",

                                "toStatus",
                                newStatus
                        );

                notificationService.sendEmail(
                        "DEFECT_UPDATED",
                        currentAssignedEmployee.getEmail(),
                        variables
                );
            }
        }


        return defectMapper.toResponse(updatedDefect);
    }

    @Override
    public Page<DefectResponseDTO> GetDefectAll(
            Pageable pageable) {

        return defectRepository
                .findAll(pageable)
                .map(defectMapper::toResponse);
    }


    // =========================================================
    // GET DEFECT BY ID
    // =========================================================

    @Override
    public DefectResponseDTO getDefectById(
            Long defectId) {

        Defect defect =
                defectRepository.findById(defectId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        ValidationMessages.INVALID_ID
                                                + defectId
                                )
                        );

        return defectMapper.toResponse(defect);
    }


    // =========================================================
    // GET DEFECTS BY PROJECT
    // =========================================================

    @Override
    public Page<DefectResponseDTO> getProjectId_Defect(
            Long proId,
            Pageable pageable) {

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "defectId")
        );


        Page<Defect> defects =
                defectRepository
                        .findByProjectDetailsProjectId(
                                proId,
                                sortedPageable
                        );

        if (defects.isEmpty()) {

            throw new ResourceNotFoundException(
                    ValidationMessages.INVALID_ID + proId
            );
        }

        return defects.map(
                defectMapper::toResponse
        );
    }


    // =========================================================
    // GET BY STATUS
    // =========================================================

    @Override
    public List<DefectResponseDTO> getDefectByStatusTypeId(
            Long statusTypeId) {

        List<Defect> defects =
                defectRepository
                        .findByStatusType_statusTypeId(
                                statusTypeId
                        );

        if (defects.isEmpty()) {

            throw new ResourceNotFoundException(
                    ValidationMessages.INVALID_ID
                            + statusTypeId
            );
        }

        return defects.stream()
                .map(defectMapper::toResponse)
                .toList();
    }


    // =========================================================
    // GET BY SEVERITY
    // =========================================================

    @Override
    public List<DefectResponseDTO> getDefectByseverityId(
            Long severityId) {

        List<Defect> defects =
                defectRepository
                        .findBySeverity_SeverityId(
                                severityId
                        );

        if (defects.isEmpty()) {

            throw new ResourceNotFoundException(
                    ValidationMessages.INVALID_ID
                            + severityId
            );
        }

        return defects.stream()
                .map(defectMapper::toResponse)
                .toList();
    }


    // =========================================================
    // GET BY PRIORITY
    // =========================================================

    @Override
    public List<DefectResponseDTO> getDefectBypriorityId(
            Long priorityId) {

        List<Defect> defects =
                defectRepository
                        .findByPriority_PriorityId(
                                priorityId
                        );

        if (defects.isEmpty()) {

            throw new ResourceNotFoundException(
                    ValidationMessages.INVALID_ID
                            + priorityId
            );
        }

        return defects.stream()
                .map(defectMapper::toResponse)
                .toList();
    }


    // =========================================================
    // GET BY DEFECT TYPE
    // =========================================================

    @Override
    public List<DefectResponseDTO> getDefectBydefectTypeId(
            Long defectTypeId) {

        List<Defect> defects =
                defectRepository
                        .findByDefectType_defectTypeId(
                                defectTypeId
                        );

        if (defects.isEmpty()) {

            throw new ResourceNotFoundException(
                    ValidationMessages.INVALID_ID
                            + defectTypeId
            );
        }

        return defects.stream()
                .map(defectMapper::toResponse)
                .toList();
    }


    // =========================================================
    // DELETE DEFECT
    // =========================================================

    @Override
    @Transactional
    public void deleteDefect(Long defectid) {

        Defect defect =
                defectRepository.findById(defectid)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        ValidationMessages.INVALID_ID
                                )
                        );

        commentRepository.deleteByDefect_DefectId(defectid);
        defectHistoryRepository.deleteByDefect_DefectId(defectid);
        if (defect.getReleaseViews() != null) {
            defect.getReleaseViews().clear();
        }

        defectRepository.delete(defect);
    }


    // =========================================================
    // FILTER DEFECTS
    // =========================================================

    @Override
    public List<DefectResponseDTO> filterDefects(
            DefectFilterDTO dto) {

        List<Defect> defects =
                defectRepository.findAll(
                        DefectSpecification.filter(dto),
                        Sort.by(
                                Sort.Direction.ASC,
                                "defectId"
                        )
                );

        return defects.stream()
                .map(defectMapper::toResponse)
                .toList();
    }

    //for dashboard -defects by module
    @Override
    public List<DefectByModuleResponseDTO> getDefectsByModule(Long projectId) {
        List<Object[]> results = defectRepository.countDefectsByModule(projectId);

        long total = results.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        List<DefectByModuleResponseDTO> dtoList = new ArrayList<>();


        //////////////////////////////////////////////////////////////////////////////////////

        for (Object[] row : results) {
            String moduleName = (String) row[0];
            Long defectCount = ((Number) row[1]).longValue();
            Double percentage = total > 0 ? (defectCount * 100.0) / total : 0.0;

            dtoList.add(new DefectByModuleResponseDTO(moduleName, defectCount, percentage));
        }

        return dtoList;
    }

    // for dashboard - Time to Find Defects (day-wise, for selected release)
    @Override
    public List<DefectDailyCountResponseDTO> getTimeToFindDefects(Long projectId, Long releaseId) {
        ReleaseView release = releaseViewRepository.findById(releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        LocalDate releaseStartDate = release.getReleaseDate();

        List<Instant> foundTimestamps =
                defectRepository.findDefectFoundTimestampsByProjectAndRelease(projectId, releaseId);

        Map<Integer, Long> countByDay = new TreeMap<>();
        for (Instant timestamp : foundTimestamps) {
            LocalDate foundDate = timestamp.atZone(ZoneId.systemDefault()).toLocalDate();
            int dayNumber = (int) ChronoUnit.DAYS.between(releaseStartDate, foundDate) + 1;
            if (dayNumber < 1) {
                dayNumber = 1;
            }
            countByDay.merge(dayNumber, 1L, Long::sum);
        }

        List<DefectDailyCountResponseDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : countByDay.entrySet()) {
            result.add(new DefectDailyCountResponseDTO("Day " + entry.getKey(), entry.getValue()));
        }
        return result;
    }

    // for dashboard - Time to Fix Defects (day-wise, for selected release)
    @Override
    public List<DefectDailyCountResponseDTO> getTimeToFixDefects(Long projectId, Long releaseId) {
        ReleaseView release = releaseViewRepository.findById(releaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        LocalDate releaseStartDate = release.getReleaseDate();

        List<LocalDate> fixedDates =
                defectHistoryRepository.findFixedDefectDatesByProjectAndRelease(projectId, releaseId);

        Map<Integer, Long> countByDay = new TreeMap<>();
        for (LocalDate fixedDate : fixedDates) {
            int dayNumber = (int) ChronoUnit.DAYS.between(releaseStartDate, fixedDate) + 1;
            if (dayNumber < 1) {
                dayNumber = 1;
            }
            countByDay.merge(dayNumber, 1L, Long::sum);
        }

        List<DefectDailyCountResponseDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : countByDay.entrySet()) {
            result.add(new DefectDailyCountResponseDTO("Day " + entry.getKey(), entry.getValue()));
        }
        return result;
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public DefectSeverityBreakdownResponseDTO getDefectSeverityBreakdown(Long projectId) {
        // 1. Verify project exists
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        // 2. Fetch all defects belonging to this project
        List<Defect> projectDefects = defectRepository.findByProjectDetailsProjectId(projectId);

        // 3. Fetch all Severities (ordered by weight) and all StatusTypes
        List<Severity> severities = severityRepository.findAllByOrderByWeightAsc();
        List<StatusType> statusTypes = statusTypeRepository.findAll();

        // 4. Calculate total remarks vs total defects across the project
        long totalRemark = projectDefects.size();
        long totalDefects = 0;

        for (Defect defect : projectDefects) {
            String statusName = defect.getStatusType() != null
                    ? defect.getStatusType().getStatusName()
                    : null;

            boolean isExcluded = statusName != null &&
                    (statusName.equalsIgnoreCase("Duplicate") ||
                            statusName.equalsIgnoreCase("Rejected"));

            if (!isExcluded) {
                totalDefects++;
            }
        }

        // 5. Build breakdown per severity
        List<SeverityBreakdownItemDTO> severityBreakdownList = new ArrayList<>();

        for (Severity severity : severities) {
            SeverityBreakdownItemDTO item = new SeverityBreakdownItemDTO();
            item.setSeverityId(severity.getSeverityId());
            item.setSeverityName(severity.getSeverityName());
            item.setSeverityColor(severity.getColorCode());

            // Initialize all status types with 0 count
            Map<String, Long> statusCounts = new LinkedHashMap<>();
            for (StatusType st : statusTypes) {
                if (st.getStatusName() != null && !st.getStatusName().isBlank()) {
                    statusCounts.put(st.getStatusName(), 0L);
                }
            }

            long severityTotalDefects = 0;

            // Count defects for this severity and each status type
            for (Defect defect : projectDefects) {
                if (defect.getSeverity() != null &&
                        defect.getSeverity().getSeverityId() != null &&
                        defect.getSeverity().getSeverityId().equals(severity.getSeverityId())) {

                    if (defect.getStatusType() != null && defect.getStatusType().getStatusName() != null) {
                        String statusName = defect.getStatusType().getStatusName();
                        statusCounts.put(statusName, statusCounts.getOrDefault(statusName, 0L) + 1L);
                        severityTotalDefects++;
                    }
                }
            }

            item.setTotalDefects(severityTotalDefects);
            item.setStatusCounts(statusCounts);
            severityBreakdownList.add(item);
        }

        // 6. Build final response
        DefectSeverityBreakdownResponseDTO response = new DefectSeverityBreakdownResponseDTO();
        response.setTotalRemark(totalRemark);
        response.setTotalDefects(totalDefects);
        response.setSeverities(severityBreakdownList);

        return response;
    }
}