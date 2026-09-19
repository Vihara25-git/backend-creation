package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.RoleBasedPreferenceRequestDTO;
import com.sgic.defect_tracker.dtos.request.RoleNotificationUpdateRequestDTO;
import com.sgic.defect_tracker.dtos.response.RoleBasedPreferenceResponseDTO;
import com.sgic.defect_tracker.entities.EmailTemplate;
import com.sgic.defect_tracker.entities.Role;
import com.sgic.defect_tracker.entities.RoleBasedPreference;
import com.sgic.defect_tracker.exceptionHandlers.ResourceNotFoundException;
import com.sgic.defect_tracker.repositories.EmailTemplateRepository;
import com.sgic.defect_tracker.repositories.RoleBasedPreferenceRepository;
import com.sgic.defect_tracker.repositories.RoleRepository;
import com.sgic.defect_tracker.service.RoleBasedPreferenceService;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
//import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleBasedPreferenceServiceImpl
        implements RoleBasedPreferenceService {

    private final RoleBasedPreferenceRepository rolePreferenceRepository;
    private final RoleRepository roleRepository;
    private final EmailTemplateRepository emailTemplateRepository;

    @Override
    public RoleBasedPreferenceResponseDTO create(
            RoleBasedPreferenceRequestDTO request
    ) {
        Role role = getRole(request.getRoleId());
        EmailTemplate template = getTemplate(request.getTemplateId());

        if (rolePreferenceRepository
                .existsByRole_RoleIdAndEmailTemplate_TemplateId(
                        request.getRoleId(),
                        request.getTemplateId()
                )) {
            throw new RuntimeException(
                    "This email template is already assigned to this role"
            );
        }

        RoleBasedPreference preference = new RoleBasedPreference();

        preference.setRole(role);
        preference.setEmailTemplate(template);
        preference.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : true
        );
        preference.setChannel(normalizeChannel(request.getChannel()));

        return mapToResponse(
                rolePreferenceRepository.save(preference)
        );
    }

    @Override
    @Transactional
    public List<RoleBasedPreferenceResponseDTO> getAll() {
        return rolePreferenceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoleBasedPreferenceResponseDTO getById(Long id) {
        RoleBasedPreference preference =
                rolePreferenceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Role preference not found"
                                )
                        );

        return mapToResponse(preference);
    }

    @Override
    public RoleBasedPreferenceResponseDTO update(
            Long id,
            RoleBasedPreferenceRequestDTO request
    ) {
        RoleBasedPreference preference =
                rolePreferenceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Role preference not found"
                                )
                        );

        preference.setRole(getRole(request.getRoleId()));
        preference.setEmailTemplate(getTemplate(request.getTemplateId()));

        if (request.getStatus() != null) {
            preference.setStatus(request.getStatus());
        }

        if (request.getChannel() != null) {
            preference.setChannel(
                    normalizeChannel(request.getChannel())
            );
        }

        return mapToResponse(
                rolePreferenceRepository.save(preference)
        );
    }

    @Override
    public void delete(Long id) {
        RoleBasedPreference preference =
                rolePreferenceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Role preference not found"
                                )
                        );

        rolePreferenceRepository.delete(preference);
    }

    @Override
    @Transactional
    public List<RoleBasedPreferenceResponseDTO> getByRoleId(
            Long roleId
    ) {
        return rolePreferenceRepository
                .findByRole_RoleIdAndStatusTrue(roleId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * Used by:
     * GET /role/{roleId}/assigned-points
     */
    @Override
    @Transactional
    public Map<Long, String> getRoleChannels(Long roleId) {
        return rolePreferenceRepository
                .findByRole_RoleId(roleId)
                .stream()
                .filter(preference ->
                        Boolean.TRUE.equals(preference.getStatus())
                )
                .collect(Collectors.toMap(
                        preference ->
                                preference.getEmailTemplate()
                                        .getTemplateId(),
                        preference ->
                                normalizeChannel(
                                        preference.getChannel()
                                )
                ));
    }

    /*
     * Used by:
     * PUT /role-notifications/update
     */
    @Override
    public void updateRoleChannels(
            RoleNotificationUpdateRequestDTO request
    ) {
        if (request.getRoleId() == null) {
            throw new IllegalArgumentException(
                    "roleId is required"
            );
        }

        if (request.getPointChannels() == null) {
            throw new IllegalArgumentException(
                    "pointChannels is required"
            );
        }

        Role role = getRole(request.getRoleId());

        /*
         * Remove old assignments for this role.
         */
        rolePreferenceRepository.deleteByRole_RoleId(
                request.getRoleId()
        );

        rolePreferenceRepository.flush();
        /*
         * Save current frontend selections.
         */
        for (Map.Entry<Long, String> entry :
                request.getPointChannels().entrySet()) {

            Long templateId = entry.getKey();
            String channel = normalizeChannel(entry.getValue());

            /*
             * "none" means no role assignment.
             */
            if ("none".equals(channel)) {
                continue;
            }

            EmailTemplate template = getTemplate(templateId);

            RoleBasedPreference preference =
                    new RoleBasedPreference();

            preference.setRole(role);
            preference.setEmailTemplate(template);
            preference.setStatus(true);
            preference.setChannel(channel);

            rolePreferenceRepository.save(preference);
        }
    }

    private Role getRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found"
                        )
                );
    }

    private EmailTemplate getTemplate(Long templateId) {
        return emailTemplateRepository.findById(templateId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Email template not found"
                        )
                );
    }

    private String normalizeChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return "none";
        }

        String normalized = channel.toLowerCase();

        if (!normalized.equals("none")
                && !normalized.equals("email")
                && !normalized.equals("whatsapp")
                && !normalized.equals("both")) {
            throw new IllegalArgumentException(
                    "Invalid notification channel: " + channel
            );
        }

        return normalized;
    }


    @Transactional(readOnly = true)
    public Optional<String> getChannel(
            Long roleId,
            Long templateId
    ) {
        return rolePreferenceRepository
                .findByRole_RoleIdAndEmailTemplate_TemplateId(
                        roleId,
                        templateId
                )
                .filter(preference ->
                        Boolean.TRUE.equals(preference.getStatus())
                )
                .filter(preference ->
                        Boolean.TRUE.equals(
                                preference.getEmailTemplate().getStatus()
                        )
                )
                .map(preference ->
                        preference.getChannel().toLowerCase()
                );
    }

    private RoleBasedPreferenceResponseDTO mapToResponse(
            RoleBasedPreference preference
    ) {
        RoleBasedPreferenceResponseDTO response =
                new RoleBasedPreferenceResponseDTO();

        response.setRoleBasedId(
                preference.getRoleBasedId()
        );
        response.setRoleId(
                preference.getRole().getRoleId()
        );
        response.setRoleName(
                preference.getRole().getRoleName()
        );
        response.setTemplateId(
                preference.getEmailTemplate().getTemplateId()
        );
        response.setEmailNotificationType(
                preference.getEmailTemplate()
                        .getEmailNotificationType()
        );
        response.setStatus(
                preference.getStatus()
        );
        response.setChannel(
                normalizeChannel(preference.getChannel())
        );

        return response;
    }
}