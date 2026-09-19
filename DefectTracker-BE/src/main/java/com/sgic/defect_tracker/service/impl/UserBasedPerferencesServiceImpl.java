package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.UserBasedPerferencesRequestDTO;
import com.sgic.defect_tracker.dtos.request.UserNotificationUpdateRequestDTO;
import com.sgic.defect_tracker.dtos.response.UserBasedPerferencesResponseDTO;
import com.sgic.defect_tracker.entities.BenchAllocation;
import com.sgic.defect_tracker.entities.EmailTemplate;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.RoleBasedPreference;
import com.sgic.defect_tracker.entities.UserBasedPerferences;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.repositories.BenchAllocationRepository;
import com.sgic.defect_tracker.repositories.EmailTemplateRepository;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.repositories.RoleBasedPreferenceRepository;
import com.sgic.defect_tracker.repositories.UserBasedPerferencesRepository;
import com.sgic.defect_tracker.service.UserBasedPerferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tools.jackson.databind.type.LogicalType.Map;

@Service
@RequiredArgsConstructor
public class UserBasedPerferencesServiceImpl
        implements UserBasedPerferencesService {

    private final UserBasedPerferencesRepository userPreferenceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final BenchAllocationRepository benchAllocationRepository;
    private final RoleBasedPreferenceRepository rolePreferenceRepository;


    // =========================================================
    // CREATE
    // =========================================================

    @Override
    @Transactional
    public UserBasedPerferencesResponseDTO create(
            UserBasedPerferencesRequestDTO request) {

        // 1. Check Employee
        Employee employee =
                employeeRepository.findById(request.getEmpId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee not found"
                                ));


        // 2. Check Employee Email
        if (employee.getEmail() == null ||
                employee.getEmail().isBlank()) {

            throw new RuntimeException(
                    "Employee email is not available"
            );
        }


        // 3. Check Email Template
        EmailTemplate template =
                emailTemplateRepository.findById(
                                request.getTemplateId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Email template not found"
                                ));


        // 4. Email Template must be ENABLED
        if (!Boolean.TRUE.equals(template.getStatus())) {

            throw new RuntimeException(
                    "Email template is disabled"
            );
        }


        // 5. Check User Preference duplicate
        boolean alreadyExists =
                userPreferenceRepository
                        .existsByEmployee_EmpIdAndEmailTemplate_TemplateId(
                                request.getEmpId(),
                                request.getTemplateId()
                        );

        if (alreadyExists) {

            throw new RuntimeException(
                    "This email template is already assigned " +
                            "to this employee"
            );
        }


        // 6. Get all current roles of employee
        List<BenchAllocation> allocations =
                benchAllocationRepository
                        .findCurrentAllocationsByEmployeeId(
                                request.getEmpId()

                        );


        // 7. Check ALL roles
        for (BenchAllocation allocation : allocations) {

            if (allocation.getRole() == null) {
                continue;
            }

            Long roleId =
                    allocation.getRole().getRoleId();


            // Get enabled role preferences
            List<RoleBasedPreference> rolePreferences =
                    rolePreferenceRepository
                            .findEnabledTemplatesByRoleId(
                                    roleId
                            );


            // 8. Check requested template
            //    already assigned through role
            for (RoleBasedPreference rolePreference :
                    rolePreferences) {

                if (rolePreference.getEmailTemplate()
                        .getTemplateId()
                        .equals(request.getTemplateId())) {

                    throw new RuntimeException(
                            "This email template is already " +
                                    "assigned to this employee through role: "
                                    + allocation.getRole().getRoleName()
                    );
                }
            }
        }


        // 9. Create User Preference
        UserBasedPerferences preference =
                new UserBasedPerferences();

        preference.setEmployee(employee);

        preference.setEmailTemplate(template);

        preference.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : true
        );


        // 10. Save
        UserBasedPerferences saved =
                userPreferenceRepository.save(
                        preference
                );


        // 11. Response
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public Map<Long, String> getUserChannels(Long userId) {
        return userPreferenceRepository.findByEmployee_EmpId(userId)
                .stream()
                .filter(preference -> Boolean.TRUE.equals(preference.getStatus()))
                .collect(Collectors.toMap(
                        preference -> preference.getEmailTemplate().getTemplateId(),
                        preference -> preference.getChannel().toLowerCase()
                ));
    }

    @Override
    @Transactional
    public void updateUserChannels(
            UserNotificationUpdateRequestDTO request
    ) {
        Employee employee = employeeRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        userPreferenceRepository.deleteByEmployee_EmpId(request.getUserId());

        if (request.getPointChannels() == null) {
            return;
        }

        for (Map.Entry<Long, String> entry :
                request.getPointChannels().entrySet()) {

            String channel = entry.getValue().toLowerCase();

            if ("none".equals(channel)) {
                continue;
            }

            EmailTemplate template = emailTemplateRepository
                    .findById(entry.getKey())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Email template not found")
                    );

            UserBasedPerferences preference = new UserBasedPerferences();
            preference.setEmployee(employee);
            preference.setEmailTemplate(template);
            preference.setStatus(true);
            preference.setChannel(channel);

            userPreferenceRepository.save(preference);
        }
    }




    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    public List<UserBasedPerferencesResponseDTO> getAll() {

        return userPreferenceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public UserBasedPerferencesResponseDTO getById(
            Long receiverId) {

        UserBasedPerferences preference =
                userPreferenceRepository.findById(receiverId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User preference not found with id: "
                                                + receiverId
                                ));

        return mapToResponse(preference);
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    @Transactional
    public UserBasedPerferencesResponseDTO update(
            Long receiverId,
            UserBasedPerferencesRequestDTO request) {

        // 1. Find existing preference
        UserBasedPerferences preference =
                userPreferenceRepository.findById(receiverId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User preference not found"
                                ));


        // 2. Find employee
        Employee employee =
                employeeRepository.findById(
                                request.getEmpId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee not found"
                                ));


        // 3. Check employee email
        if (employee.getEmail() == null ||
                employee.getEmail().isBlank()) {

            throw new RuntimeException(
                    "Employee email is not available"
            );
        }


        // 4. Find template
        EmailTemplate template =
                emailTemplateRepository.findById(
                                request.getTemplateId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Email template not found"
                                ));


        // 5. Template must be enabled
        if (!Boolean.TRUE.equals(template.getStatus())) {

            throw new RuntimeException(
                    "Email template is disabled"
            );
        }


        // 6. Check duplicate
        boolean employeeChanged =
                !preference.getEmployee()
                        .getEmpId()
                        .equals(request.getEmpId());

        boolean templateChanged =
                !preference.getEmailTemplate()
                        .getTemplateId()
                        .equals(request.getTemplateId());


        if (employeeChanged || templateChanged) {

            boolean alreadyExists =
                    userPreferenceRepository
                            .existsByEmployee_EmpIdAndEmailTemplate_TemplateId(
                                    request.getEmpId(),
                                    request.getTemplateId()
                            );

            if (alreadyExists) {

                throw new RuntimeException(
                        "This email template is already assigned " +
                                "to this employee"
                );
            }
        }


        // 7. Get employee current roles
        List<BenchAllocation> allocations =
                benchAllocationRepository
                        .findCurrentAllocationsByEmployeeId(
                                request.getEmpId()
                        );


        // 8. Check all roles
        for (BenchAllocation allocation : allocations) {

            if (allocation.getRole() == null) {
                continue;
            }

            Long roleId =
                    allocation.getRole().getRoleId();


            List<RoleBasedPreference> rolePreferences =
                    rolePreferenceRepository
                            .findEnabledTemplatesByRoleId(
                                    roleId
                            );


            // 9. Check role-based template
            for (RoleBasedPreference rolePreference :
                    rolePreferences) {

                if (rolePreference.getEmailTemplate()
                        .getTemplateId()
                        .equals(request.getTemplateId())) {

                    throw new RuntimeException(
                            "This email template is already " +
                                    "assigned to this employee through role: "
                                    + allocation.getRole().getRoleName()
                    );
                }
            }
        }


        // 10. Update
        preference.setEmployee(employee);
        preference.setEmailTemplate(template);

        if (request.getStatus() != null) {
            preference.setStatus(
                    request.getStatus()
            );
        }


        // 11. Save
        UserBasedPerferences updated =
                userPreferenceRepository.save(
                        preference
                );


        return mapToResponse(updated);
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    @Transactional
    public void delete(Long receiverId) {

        UserBasedPerferences preference =
                userPreferenceRepository.findById(receiverId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User preference not found"
                                ));


        userPreferenceRepository.delete(preference);
    }


    // =========================================================
    // RESPONSE MAPPER
    // =========================================================

    private UserBasedPerferencesResponseDTO mapToResponse(
            UserBasedPerferences preference) {

        UserBasedPerferencesResponseDTO response =
                new UserBasedPerferencesResponseDTO();


        response.setReceiverId(
                preference.getReceiverId()
        );


        response.setEmpId(
                preference.getEmployee().getEmpId()
        );


        response.setEmployeeName(
                preference.getEmployee().getFirstName()
                        + " "
                        + preference.getEmployee().getLastName()
        );


        response.setTemplateId(
                preference.getEmailTemplate()
                        .getTemplateId()
        );


        response.setEmailNotificationType(
                preference.getEmailTemplate()
                        .getEmailNotificationType()
        );


        response.setStatus(
                preference.getStatus()
        );


        return response;
    }
}