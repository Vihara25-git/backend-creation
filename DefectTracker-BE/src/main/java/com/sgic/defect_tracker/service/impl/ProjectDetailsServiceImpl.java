package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.ProjectDetailsRequestDto;
import com.sgic.defect_tracker.dtos.response.ProjectDetailsResponseDto;
import com.sgic.defect_tracker.entities.BenchAvailabilityView;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.entities.ProjectDetails;
import com.sgic.defect_tracker.entities.Role;
import com.sgic.defect_tracker.mapper.ProjectDetailsMapper;
import com.sgic.defect_tracker.repositories.BenchAllocationRepository;
import com.sgic.defect_tracker.repositories.BenchAvailabilityViewRepository;
import com.sgic.defect_tracker.repositories.DefectRepository;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.repositories.ProjectDetailsRepository;
import com.sgic.defect_tracker.repositories.RoleRepository;
import com.sgic.defect_tracker.service.EmailNotificationService;
import com.sgic.defect_tracker.service.ProjectDetailsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sgic.defect_tracker.entities.BenchAllocation;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjectDetailsServiceImpl implements ProjectDetailsService {

    @Autowired
    private ProjectDetailsRepository projectDetailsRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectDetailsMapper projectDetailsMapper;

    @Autowired
    private EmailNotificationService notificationService;

    @Autowired
    private BenchAllocationRepository benchAllocationRepository;

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BenchAvailabilityViewRepository benchAvailabilityRepository;


    private String normalize(String name) {
        return name.replaceAll("\\s+", "").toLowerCase();
    }

    private Role getProjectManagerRole() {
        List<Role> roles = roleRepository.findProjectManagerRoles();
        if (!roles.isEmpty()) {
            return roles.get(0);
        }

        List<Role> allRoles = roleRepository.findAll();

        Optional<Role> matchingRole = allRoles.stream()
                .filter(r -> (r.getRoleType() != null && (r.getRoleType().equalsIgnoreCase("PROJECT_MANAGER") || r.getRoleType().toUpperCase().contains("MANAGER") || r.getRoleType().toUpperCase().contains("PM")))
                        || (r.getRoleName() != null && (r.getRoleName().equalsIgnoreCase("Project Manager") || r.getRoleName().toUpperCase().contains("MANAGER") || r.getRoleName().toUpperCase().contains("PM"))))
                .findFirst();

        if (matchingRole.isPresent()) {
            return matchingRole.get();
        }

        // If no Project Manager role exists in database, auto-create it
        try {
            Role newRole = new Role();
            newRole.setRoleName("Project Manager");
            newRole.setRoleType("PROJECT_MANAGER");
            return roleRepository.save(newRole);
        } catch (Exception e) {
            List<Role> reloadedRoles = roleRepository.findAll();
            if (!reloadedRoles.isEmpty()) {
                return reloadedRoles.get(0);
            }
            return null;
        }
    }

    private ProjectDetailsResponseDto convertToResponseDto(ProjectDetails project) {
        if (project == null) return null;
        ProjectDetailsResponseDto dto = projectDetailsMapper.toResponseDto(project);
        if (project.getProjectId() != null) {
            List<BenchAllocation> pmAllocations = benchAllocationRepository.findProjectManagerAllocationsByProjectId(project.getProjectId());
            if (!pmAllocations.isEmpty() && pmAllocations.get(0).getAvailability() != null) {
                dto.setManagerAllocation(pmAllocations.get(0).getAvailability().intValue());
            }
        }
        return dto;
    }


    @Override
    @Transactional
    public ProjectDetailsResponseDto saveProject(
            ProjectDetailsRequestDto dto
    ) {
        String projectName = dto.getProjectName().trim();
        String normalizedNewName = normalize(projectName);

        List<ProjectDetails> allProjects = projectDetailsRepository.findAll();

        boolean isDuplicate = allProjects.stream()
                .anyMatch(p -> normalize(p.getProjectName())
                        .equals(normalizedNewName));

        if (isDuplicate) {
            throw new RuntimeException(
                    "Project name already exists: " + projectName
            );
        }

        if (dto.getClientDetails() != null) {

            String clientEmail = dto.getClientDetails().getEmail();
            String clientPhone = dto.getClientDetails().getPhoneNumber();

            if (clientEmail != null && employeeRepository.existsByEmail(clientEmail)) {
                throw new RuntimeException(
                        "This email is already associated with an employee. Please use a different email for the client."
                );
            }

            if (clientPhone != null && employeeRepository.existsByWhatsappNumber(clientPhone)) {
                throw new RuntimeException(
                        "This phone number is already associated with an employee. Please use a different phone number for the client."
                );
            }
        }

        Employee projectManager =
                employeeRepository.findById(dto.getProjectManagerId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found with ID: "
                                                + dto.getProjectManagerId()
                                )
                        );

        // Validate PM availability from bench view
        Long availablePercentage = 100L;
        Optional<BenchAvailabilityView> viewOpt = benchAvailabilityRepository.findByEmpId(projectManager.getEmpId());
        if (viewOpt.isPresent()) {
            availablePercentage = viewOpt.get().getAvailablePercentage() != null ? viewOpt.get().getAvailablePercentage() : 100L;
        }

        if (availablePercentage <= 0) {
            throw new RuntimeException(
                    "Selected Project Manager " + projectManager.getFirstName() + " " + projectManager.getLastName() + " has 0% availability (already fully allocated)."
            );
        }

        Long requestedAllocation = dto.getManagerAllocation() != null && dto.getManagerAllocation() > 0
                ? dto.getManagerAllocation().longValue()
                : Math.min(100L, availablePercentage);

        if (requestedAllocation > availablePercentage) {
            throw new RuntimeException(
                    "Selected Project Manager only has " + availablePercentage + "% availability, cannot allocate " + requestedAllocation + "%."
            );
        }

        ProjectDetails projectDetails =
                projectDetailsMapper.toEntity(dto);

        projectDetails.setStatus("Active");
        projectDetails.setProjectManager(projectManager);

        ProjectDetails savedProject =
                projectDetailsRepository.save(projectDetails);

        // Create BenchAllocation for the Project Manager
        LocalDateTime startDateTime = dto.getStartDate() != null ? dto.getStartDate().atStartOfDay() : LocalDateTime.now();
        LocalDateTime endDateTime = dto.getEndDate() != null ? dto.getEndDate().atTime(23, 59, 59, 999999) : startDateTime.plusYears(1);

        Role pmRole = getProjectManagerRole();

        BenchAllocation pmAllocation = new BenchAllocation();
        pmAllocation.setEmployee(projectManager);
        pmAllocation.setEmployeeName(projectManager.getFirstName() + " " + projectManager.getLastName());
        pmAllocation.setEmployeeEmail(projectManager.getEmail());
        pmAllocation.setRole(pmRole);
        pmAllocation.setProjectDetails(savedProject);
        pmAllocation.setAvailability(requestedAllocation);
        pmAllocation.setStartDate(startDateTime);
        pmAllocation.setEndDate(endDateTime);
        benchAllocationRepository.save(pmAllocation);

        notificationService.sendEmail(
                "PROJECT_CREATED",
                projectManager.getEmail(),
                Map.of(
                        "projectName",
                        savedProject.getProjectName(),

                        "projectManager",
                        projectManager.getFirstName()
                                + " "
                                + projectManager.getLastName()
                )
        );

        return convertToResponseDto(savedProject);
    }


    @Override
    @Transactional
    public ProjectDetailsResponseDto updateProject(
            Long projectId,
            ProjectDetailsRequestDto dto
    ) {

        ProjectDetails existingProject =
                projectDetailsRepository.findById(projectId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Project not found with ID: "
                                                + projectId
                                )
                        );

        String projectName = dto.getProjectName().trim();
        String normalizedNewName = normalize(projectName);

        List<ProjectDetails> allProjects = projectDetailsRepository.findAll();

        boolean isDuplicate = allProjects.stream()
                .filter(p -> !p.getProjectId().equals(projectId))
                .anyMatch(p -> normalize(p.getProjectName())
                        .equals(normalizedNewName));

        if (isDuplicate) {
            throw new RuntimeException(
                    "Project name already exists: " + projectName
            );
        }

        Employee projectManager =
                employeeRepository.findById(dto.getProjectManagerId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Employee not found with ID: "
                                                + dto.getProjectManagerId()
                                )
                        );

        String oldStatus = existingProject.getStatus();

        projectDetailsMapper.updateEntityFromDto(
                dto,
                existingProject
        );

        String newStatus = dto.getStatus();


        if (!"COMPLETED".equalsIgnoreCase(oldStatus)
                && "COMPLETED".equalsIgnoreCase(newStatus)) {

            long openDefectCount =
                    defectRepository.countOpenDefectsByProjectId(projectId);

            if (openDefectCount > 0) {
                throw new RuntimeException(
                        "Cannot mark project as Completed. There are "
                                + openDefectCount
                                + " active defect(s) in this project."
                );
            }
        }

        Employee oldProjectManager = existingProject.getProjectManager();
        boolean pmChanged = oldProjectManager == null || !oldProjectManager.getEmpId().equals(projectManager.getEmpId());

        existingProject.setProjectManager(projectManager);
        existingProject.setStatus(newStatus);

        LocalDateTime startDateTime = dto.getStartDate() != null ? dto.getStartDate().atStartOfDay() : existingProject.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = dto.getEndDate() != null ? dto.getEndDate().atTime(23, 59, 59, 999999) : existingProject.getEndDate().atTime(23, 59, 59, 999999);

        List<BenchAllocation> existingPmAllocations = benchAllocationRepository.findProjectManagerAllocationsByProjectId(projectId);
        Role pmRole = getProjectManagerRole();

        if (pmChanged) {
            LocalDateTime now = LocalDateTime.now();
            for (BenchAllocation oldAlloc : existingPmAllocations) {
                if (oldAlloc.getEndDate() == null || oldAlloc.getEndDate().isAfter(now)) {
                    oldAlloc.setEndDate(now);
                }
            }
            if (!existingPmAllocations.isEmpty()) {
                benchAllocationRepository.saveAll(existingPmAllocations);
            }

            Long availablePercentage = 100L;
            Optional<BenchAvailabilityView> viewOpt = benchAvailabilityRepository.findByEmpId(projectManager.getEmpId());
            if (viewOpt.isPresent()) {
                availablePercentage = viewOpt.get().getAvailablePercentage() != null ? viewOpt.get().getAvailablePercentage() : 100L;
            }

            if (availablePercentage <= 0) {
                throw new RuntimeException(
                        "Selected Project Manager " + projectManager.getFirstName() + " " + projectManager.getLastName() + " has 0% availability (already fully allocated)."
                );
            }

            Long requestedAllocation = dto.getManagerAllocation() != null && dto.getManagerAllocation() > 0
                    ? dto.getManagerAllocation().longValue()
                    : Math.min(100L, availablePercentage);

            if (requestedAllocation > availablePercentage) {
                throw new RuntimeException(
                        "Selected Project Manager only has " + availablePercentage + "% availability, cannot allocate " + requestedAllocation + "%."
                );
            }

            BenchAllocation newPmAllocation = new BenchAllocation();
            newPmAllocation.setEmployee(projectManager);
            newPmAllocation.setEmployeeName(projectManager.getFirstName() + " " + projectManager.getLastName());
            newPmAllocation.setEmployeeEmail(projectManager.getEmail());
            newPmAllocation.setRole(pmRole);
            newPmAllocation.setProjectDetails(existingProject);
            newPmAllocation.setAvailability(requestedAllocation);
            newPmAllocation.setStartDate(startDateTime);
            newPmAllocation.setEndDate(endDateTime);
            benchAllocationRepository.save(newPmAllocation);
        } else {
            Long currentAllocation = existingPmAllocations.isEmpty() ? 0L : (existingPmAllocations.get(0).getAvailability() != null ? existingPmAllocations.get(0).getAvailability() : 0L);
            Long targetAllocation = dto.getManagerAllocation() != null && dto.getManagerAllocation() > 0
                    ? dto.getManagerAllocation().longValue()
                    : (currentAllocation > 0 ? currentAllocation : 100L);

            if (targetAllocation > currentAllocation) {
                Long additionalNeeded = targetAllocation - currentAllocation;
                Long freeAvailability = 100L;
                Optional<BenchAvailabilityView> viewOpt = benchAvailabilityRepository.findByEmpId(projectManager.getEmpId());
                if (viewOpt.isPresent()) {
                    freeAvailability = viewOpt.get().getAvailablePercentage() != null ? viewOpt.get().getAvailablePercentage() : 100L;
                }
                if (additionalNeeded > freeAvailability) {
                    throw new RuntimeException("Selected Project Manager only has " + freeAvailability + "% free availability, cannot allocate " + targetAllocation + "%.");
                }
            }

            if (!existingPmAllocations.isEmpty()) {
                BenchAllocation pmAlloc = existingPmAllocations.get(0);
                pmAlloc.setAvailability(targetAllocation);
                pmAlloc.setStartDate(startDateTime);
                pmAlloc.setEndDate(endDateTime);
                benchAllocationRepository.save(pmAlloc);
                if (existingPmAllocations.size() > 1) {
                    benchAllocationRepository.deleteAll(existingPmAllocations.subList(1, existingPmAllocations.size()));
                }
            } else {
                BenchAllocation newPmAllocation = new BenchAllocation();
                newPmAllocation.setEmployee(projectManager);
                newPmAllocation.setEmployeeName(projectManager.getFirstName() + " " + projectManager.getLastName());
                newPmAllocation.setEmployeeEmail(projectManager.getEmail());
                newPmAllocation.setRole(pmRole);
                newPmAllocation.setProjectDetails(existingProject);
                newPmAllocation.setAvailability(targetAllocation);
                newPmAllocation.setStartDate(startDateTime);
                newPmAllocation.setEndDate(endDateTime);
                benchAllocationRepository.save(newPmAllocation);
            }
        }

        if (!"COMPLETED".equalsIgnoreCase(oldStatus)
                && "COMPLETED".equalsIgnoreCase(newStatus)) {

            LocalDateTime completionDateTime = LocalDateTime.now();

            List<BenchAllocation> allocations =
                    benchAllocationRepository
                            .findByProjectDetails_ProjectId(
                                    projectId,
                                    completionDateTime
                            );

            for (BenchAllocation allocation : allocations) {
                allocation.setEndDate(completionDateTime);
            }

            benchAllocationRepository.saveAll(allocations);
        }

        ProjectDetails updatedProject =
                projectDetailsRepository.save(existingProject);

        // Send project updated email
        notificationService.sendEmail(
                "PROJECT_UPDATED",
                projectManager.getEmail(),
                Map.of(
                        "projectName",
                        updatedProject.getProjectName(),

                        "projectManager",
                        projectManager.getFirstName()
                                + " "
                                + projectManager.getLastName(),

                        "status",
                        updatedProject.getStatus(),

                        "updatedAt",
                        LocalDateTime.now()
                )
        );
        return convertToResponseDto(updatedProject);
    }


    @Transactional
    @Override
    public void deleteProject(Long projectId) {

        ProjectDetails project =
                projectDetailsRepository.findById(projectId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Project not found with ID: "
                                                + projectId
                                )
                        );

        if ("Active".equalsIgnoreCase(project.getStatus())) {
            throw new RuntimeException(
                    "Active project cannot be deleted............"
            );
        }

        if ("On Hold".equalsIgnoreCase(project.getStatus())) {
            throw new RuntimeException(
                    "On Hold project cannot be deleted."
            );
        }

        benchAllocationRepository.deleteByProjectDetails_ProjectId(projectId);

        projectDetailsRepository.delete(project);
    }


    @Override
    public ProjectDetailsResponseDto getProjectById(Long id) {

        ProjectDetails project =
                projectDetailsRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Project not found with ID: " + id
                                )
                        );

        return convertToResponseDto(project);
    }


    @Override
    public List<ProjectDetailsResponseDto> filterProjects(
            String status,
            String search
    ) {

        if (status == null || status.trim().isEmpty()) {
            status = "";
        }

        if (search == null || search.trim().isEmpty()) {
            search = "";
        }

        return projectDetailsRepository
                .filterProjects(status, search)
                .stream()
                .map(this::convertToResponseDto)
                .toList();
    }
}