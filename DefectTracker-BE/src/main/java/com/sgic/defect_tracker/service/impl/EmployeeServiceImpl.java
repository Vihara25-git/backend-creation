package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.ChangePasswordRequestDTO;
import com.sgic.defect_tracker.dtos.request.ChangePasswordRequestDTO;
import com.sgic.defect_tracker.dtos.request.EmployeeRequestDTO;
import com.sgic.defect_tracker.dtos.response.EmployeeResponseDTO;
import com.sgic.defect_tracker.entities.Designation;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.mapper.EmployeeMapper;
import com.sgic.defect_tracker.repositories.BenchAvailabilityViewRepository;
import com.sgic.defect_tracker.repositories.DesignationRepository;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.service.EmailConfigService;
import com.sgic.defect_tracker.service.EmailNotificationService;
import com.sgic.defect_tracker.service.EmployeeService;
import com.sgic.defect_tracker.utils.PasswordGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.sgic.defect_tracker.repositories.BenchAllocationRepository;
import com.sgic.defect_tracker.repositories.ProjectDetailsRepository;
import org.springframework.data.domain.Page;

//import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;

    private final BenchAllocationRepository benchAllocationRepository;
    private final ProjectDetailsRepository projectDetailsRepository;
    private final EmailConfigService emailConfigService;
    private final EmailNotificationService notificationService;
    private final BenchAvailabilityViewRepository benchAvailabilityRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public List<EmployeeResponseDTO> getEmployeesByDesignation(Long designationId) {

        List<Employee> employees = employeeRepository.findByDesignation_DesignationId(designationId);

        return employees.stream()
                .map(emp -> {
                    EmployeeResponseDTO dto = employeeMapper.toDto(emp);
                    if (dto != null) {
                        benchAvailabilityRepository.findByEmpId(emp.getEmpId())
                                .ifPresentOrElse(
                                        view -> dto.setAvailablePercentage(view.getAvailablePercentage()),
                                        () -> dto.setAvailablePercentage(100L)
                                );
                    }
                    return dto;
                })
                .toList();
    }
    @Override
    public List<EmployeeResponseDTO> getEmployeesByEmployee(Long empId){
        Optional<Employee> employees = employeeRepository.findById(empId);

        return employees.stream()
                .map(emp -> {
                    EmployeeResponseDTO dto = employeeMapper.toDto(emp);
                    if (dto != null) {
                        benchAvailabilityRepository.findByEmpId(emp.getEmpId())
                                .ifPresentOrElse(
                                        view -> dto.setAvailablePercentage(view.getAvailablePercentage()),
                                        () -> dto.setAvailablePercentage(100L)
                                );
                    }
                    return dto;
                })
                .toList();
    }

//    @Override
//    public List<EmployeeResponseDTO> viewEmployees() {
//        List<Employee> employees = employeeRepository.findAll();
//
//        return employees.stream()
//                .map(employeeMapper::toDto).toList();
//    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long empId) {
        return null;
    }

    @Override
    public Page<EmployeeResponseDTO> viewEmployees(Pageable pageable) {
        Page<Employee> result =
                employeeRepository.findByEmpIdNot(1L, pageable);

        return result.map(employeeMapper::toDto);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO UpdateEmployee(
            Long empId,
            EmployeeRequestDTO employeeRequestDTO) {

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employee not found"));

        Designation designation = designationRepository.findById(
                employeeRequestDTO.getDesignationId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Designation not found"));

        // =====================================================
// NORMALIZE AND VALIDATE WHATSAPP NUMBER
// =====================================================

        String whatsappNumber = employeeRequestDTO.getWhatsappNumber();

        if (whatsappNumber == null || whatsappNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Employee whatsapp number is required."
            );
        }

// Spaces and other characters are NOT allowed
        if (!whatsappNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "Employee whatsapp number must contain exactly 10 digits with no spaces."
            );
        }

        employeeRequestDTO.setWhatsappNumber(whatsappNumber);

        // Check whether email has changed
        boolean emailChanged =
                !employee.getEmail().equalsIgnoreCase(
                        employeeRequestDTO.getEmail()
                );

        // Check duplicate email only when email is changed
        if (emailChanged &&
                employeeRepository.existsByEmail(
                        employeeRequestDTO.getEmail())) {

            throw new IllegalArgumentException(
                    "Employee email already exists."
            );
        }

        // Check duplicate WhatsApp number when changed
        boolean whatsappChanged =
                !Objects.equals(
                        employee.getWhatsappNumber(),
                        employeeRequestDTO.getWhatsappNumber()
                );


        if (whatsappChanged &&
                employeeRepository.existsByWhatsappNumberAndEmpIdNot(
                        employeeRequestDTO.getWhatsappNumber(),
                        empId
                )) {

            throw new IllegalArgumentException(
                    "Employee whatsapp number already exists."
            );
        }

        // Check no changes
        if (employee.getFirstName().equals(employeeRequestDTO.getFirstName())
                && employee.getLastName().equals(employeeRequestDTO.getLastName())
                && employee.getGender().equals(employeeRequestDTO.getGender())
                && employee.getWhatsappNumber().equals(employeeRequestDTO.getWhatsappNumber())
                && employee.getEmail().equalsIgnoreCase(employeeRequestDTO.getEmail())
                && employee.getJoinDate().equals(employeeRequestDTO.getJoinDate())
                && employee.getDesignation() != null
                && employee.getDesignation()
                .getDesignationId()
                .equals(employeeRequestDTO.getDesignationId())) {

            throw new RuntimeException(
                    "No changes detected. Please update any field before saving."
            );
        }

        // Update employee details
        employee.setFirstName(
                employeeRequestDTO.getFirstName()
        );

        employee.setLastName(
                employeeRequestDTO.getLastName()
        );

        employee.setGender(
                employeeRequestDTO.getGender()
        );

        employee.setWhatsappNumber(
                employeeRequestDTO.getWhatsappNumber()
        );

        employee.setEmail(
                employeeRequestDTO.getEmail()
        );

        employee.setJoinDate(
                employeeRequestDTO.getJoinDate()
        );

        employee.setDesignation(
                designation
        );

        // Generate new password when email is changed
        String newPassword = null;

        if (emailChanged) {

            newPassword = passwordGenerator.generate();

            String encodedPassword =
                    passwordEncoder.encode(newPassword);


            employee.setPassword(encodedPassword);
        }

        // Save updated employee
        Employee saved = employeeRepository.save(employee);

        // Send email to NEW email address
        if (emailChanged) {

            Map<String, Object> variables = Map.of(
                    "employeeName",
                    saved.getFirstName()
                            + " "
                            + saved.getLastName(),

                    "email",
                    saved.getEmail(),

                    "password",
                    newPassword
            );

            notificationService.sendEmail(
                    "EMPLOYEE_EMAIL_UPDATED",
                    saved.getEmail(),
                    variables
            );
        }

        return employeeMapper.toDto(saved);
    }


    @Transactional
    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO employeeRequestDTO) {

        // =====================================================
// NORMALIZE AND VALIDATE WHATSAPP NUMBER
// =====================================================

        String whatsappNumber = employeeRequestDTO.getWhatsappNumber();

        if (whatsappNumber == null || whatsappNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Employee whatsapp number is required."
            );
        }

// Spaces and other characters are NOT allowed
        if (!whatsappNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                    "Employee whatsapp number must contain exactly 10 digits with no spaces."
            );
        }

        employeeRequestDTO.setWhatsappNumber(whatsappNumber);


        // Validate email duplicate
        if (employeeRepository.existsByEmail(employeeRequestDTO.getEmail())) {
            throw new IllegalArgumentException(
                    "Employee email already exists."
            );
        }

        // Validate whatsapp number duplicate
        if (employeeRepository.existsByWhatsappNumber(employeeRequestDTO.getWhatsappNumber())) {
            throw new IllegalArgumentException(
                    "Employee whatsapp number already exists."
            );
        }

        // Required validations
        if (employeeRequestDTO.getFirstName() == null
                || employeeRequestDTO.getFirstName().isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }

        if (employeeRequestDTO.getLastName() == null
                || employeeRequestDTO.getLastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }

        if (employeeRequestDTO.getGender() == null
                || employeeRequestDTO.getGender().isBlank()) {
            throw new IllegalArgumentException("Gender is required.");
        }

        if (employeeRequestDTO.getJoinDate() == null) {
            throw new IllegalArgumentException("Join date is required.");
        }

        if (employeeRequestDTO.getDesignationId() == null) {
            throw new IllegalArgumentException("Designation is required.");
        }

        String namePattern = "^[A-Za-z.]+$";

        if (!employeeRequestDTO.getFirstName().matches(namePattern)) {
            throw new IllegalArgumentException(
                    "First name can only contain letters and '.' (e.g. S.H.Hasan) — no spaces, numbers, or symbols."
            );
        }

        if (!employeeRequestDTO.getLastName().matches(namePattern)) {
            throw new IllegalArgumentException(
                    "Last name can only contain letters and '.' (e.g. S.H.Hasan) — no spaces, numbers, or symbols."
            );
        }


        Employee entity = employeeMapper.toEntity(employeeRequestDTO);

        Designation designation = designationRepository.findById(
                employeeRequestDTO.getDesignationId()
        ).orElseThrow(() ->
                new ResourceNotFoundException("Designation not found")
        );

        // Generate a random password for the new employee
        String rawPassword = passwordGenerator.generate();
        String encodedPassword =
                passwordEncoder.encode(rawPassword);

        entity.setPassword(encodedPassword);

        entity.setDesignation(designation);

        Employee saved = employeeRepository.save(entity);

        EmployeeResponseDTO responseDTO = employeeMapper.toDto(saved);


        // Attach plaintext password to this response only — never persisted response-side elsewhere
        responseDTO.setPassword(rawPassword);

// Prepare email template variables
        Map<String, Object> variables = Map.of(
                "employeeName",
                saved.getFirstName() + " " + saved.getLastName(),

                "email",
                saved.getEmail(),

                "password",
                rawPassword
        );

// Send employee-created email
        notificationService.sendEmail(
                "EMPLOYEE_CREATED",
                saved.getEmail(),
                variables
        );

        return responseDTO;
    }


    @Override
    @Transactional
    public void DeleteEmployee(Long empId) {

        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employee not found"));

        boolean hasActiveOrFutureAllocation =
                benchAllocationRepository.existsActiveAllocation(empId);

        if (hasActiveOrFutureAllocation) {
            throw new IllegalStateException(
                    "Employee cannot be deleted because the employee has a current or future allocation."
            );
        }

          employeeRepository.delete(employee);
//
//        boolean hasActiveAllocation =
//                benchAllocationRepository.existsActiveAllocation(empId);
//
//        if (hasActiveAllocation) {
//            throw new IllegalArgumentException(
//                    "Employee cannot be deleted because the employee has allocation records."
//            );
//        }
//
//        employeeRepository.delete(employee);
    }

    @Override
    public List<EmployeeResponseDTO> filterEmployee(
            String keyword,
            String gender,
            String status,
            String designation) {


        if(keyword != null && keyword.isBlank()){
            keyword = null;
        }



        if(status != null && status.isBlank()){
            status = null;
        }


        if(gender != null){
            gender = gender.toLowerCase();
        }

        if(status != null){
            status = status.toLowerCase();
        }
        if(designation != null && designation.isBlank()){
            designation = null;
        }


        List<Employee> employees =
                employeeRepository.filterEmployees(
                        keyword,
                        gender,
                        status,
                        designation
                );


        return employees.stream()
                .map(employeeMapper::toDto)
                .toList();
    }
    @Override
    public EmployeeResponseDTO updateEmployee(Long empId, Boolean isActive) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (Boolean.FALSE.equals(isActive)) {
            boolean isAllocatedBench = benchAllocationRepository.existsActiveAllocation(empId);
            boolean isProjectManager = projectDetailsRepository
                    .existsByProjectManager_EmpIdAndStatus(empId, "Active");

            if (isAllocatedBench || isProjectManager) {
                throw new RuntimeException(
                        "This employee is currently allocated to a project. Cannot mark as inactive."
                );
            }
        }

        employee.setIsActive(isActive);
        if (Boolean.FALSE.equals(isActive)) {

            Map<String, Object> variables = Map.of(
                    "employeeName",
                    employee.getFirstName() + " " + employee.getLastName()
            );

            notificationService.sendEmail(
                    "EMPLOYEE_DEACTIVATED",
                    employee.getEmail(),
                    variables
            );
        }

        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toDto(saved);
    }


    @Override
    @org.springframework.transaction.annotation.Transactional
    public void changePassword(ChangePasswordRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Change password request cannot be null");
        }

        Employee employee = null;

        if (requestDTO.getEmpId() != null) {
            employee = employeeRepository.findById(requestDTO.getEmpId()).orElse(null);
        }

        if (employee == null && requestDTO.getEmail() != null && !requestDTO.getEmail().isBlank()) {
            employee = employeeRepository.findByEmailIgnoreCase(requestDTO.getEmail().trim()).orElse(null);
        }

        // Fallback: extract email from the authenticated JWT token if not provided in DTO
        if (employee == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String authEmail = authentication.getName();
                if (authEmail != null && !authEmail.isBlank()) {
                    employee = employeeRepository.findByEmailIgnoreCase(authEmail.trim()).orElse(null);
                }
            }
        }

        if (employee == null) {
            throw new ResourceNotFoundException("Employee not found");
        }

        if (requestDTO.getCurrentPassword() == null || requestDTO.getCurrentPassword().isBlank()) {
            throw new IllegalArgumentException("Current password is required");
        }

        String currentPassword = requestDTO.getCurrentPassword().trim();
        String newPassword = requestDTO.getNewPassword() != null ? requestDTO.getNewPassword().trim() : "";

        boolean passwordMatches = false;
        if (employee.getPassword() != null) {
            try {
                if (passwordEncoder != null && passwordEncoder.matches(currentPassword, employee.getPassword())) {
                    passwordMatches = true;
                } else if (employee.getPassword().equals(currentPassword)) {
                    passwordMatches = true;
                }
            } catch (Exception e) {
                passwordMatches = employee.getPassword().equals(currentPassword);
            }
        }

        if (!passwordMatches) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        if (newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        if (newPassword.equals(currentPassword)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        String encodedNewPassword = passwordEncoder != null
                ? passwordEncoder.encode(newPassword)
                : newPassword;
        employee.setPassword(encodedNewPassword);
        Employee saved = employeeRepository.save(employee);


        try {
            String fullName = (saved.getFirstName() != null ? saved.getFirstName() : "") +
                    (saved.getLastName() != null && !saved.getLastName().isBlank() ? " " + saved.getLastName() : "");

            Map<String, Object> variables = Map.of(
                    "employeeName", fullName.trim().isEmpty() ? "User" : fullName.trim(),
                    "email", saved.getEmail() != null ? saved.getEmail() : ""
            );

            notificationService.sendEmail(
                    "PASSWORD_CHANGED",
                    saved.getEmail(),
                    variables
            );
        } catch (Exception e) {
            System.err.println("Could not send PASSWORD_CHANGED email notification: " + e.getMessage());
        }
    }
}