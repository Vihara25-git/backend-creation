package com.sgic.defect_tracker.config;

import com.sgic.defect_tracker.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ForgotPasswordTokenCleanupScheduler {

    private final EmployeeRepository employeeRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void clearExpiredTokens() {

        employeeRepository.clearExpiredForgotPasswordTokens(
                LocalDateTime.now()
        );
    }
}