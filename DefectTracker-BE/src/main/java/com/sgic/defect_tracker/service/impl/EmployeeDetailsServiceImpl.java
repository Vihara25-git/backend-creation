package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.enums.LoginType;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.service.EmployeeDetailsService;
import com.sgic.defect_tracker.service.PrivilegeAssignmentService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeDetailsServiceImpl implements EmployeeDetailsService {

    private final EmployeeRepository employeeRepository;
    private final PrivilegeAssignmentService privilegeAssignmentService;

    public EmployeeDetailsServiceImpl(
            EmployeeRepository employeeRepository,
            PrivilegeAssignmentService privilegeAssignmentService) {
        this.employeeRepository = employeeRepository;
        this.privilegeAssignmentService = privilegeAssignmentService;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Invalid email or password");
        }

        Employee employee = employeeRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid email or password"
                        ));

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (employee.getLoginType() == LoginType.ADMIN && !Boolean.TRUE.equals(employee.getCustomPermissions())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        try {
            Set<String> permissionCodes = privilegeAssignmentService.getEffectivePermissionCodes(employee.getEmpId());
            if (permissionCodes != null) {
                for (String code : permissionCodes) {
                    authorities.add(new SimpleGrantedAuthority(code));
                }
            }
        } catch (Exception e) {
            // fallback gracefully if permissions cannot be loaded
        }

        boolean isEnabled = employee.getIsActive() == null || Boolean.TRUE.equals(employee.getIsActive());

        return User.builder()
                .username(employee.getEmail())
                .password(employee.getPassword() != null ? employee.getPassword() : "")
                .authorities(authorities)
                .disabled(!isEnabled)
                .build();
    }
}
