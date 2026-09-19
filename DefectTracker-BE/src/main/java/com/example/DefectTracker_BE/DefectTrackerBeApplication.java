package com.example.DefectTracker_BE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.example.DefectTracker_BE", "com.sgic.defect_tracker"})
@AutoConfigurationPackage(basePackages = {"com.sgic.defect_tracker", "com.example.DefectTracker_BE"})
@EnableJpaRepositories(basePackages = "com.sgic.defect_tracker.repositories")
public class DefectTrackerBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DefectTrackerBeApplication.class, args);
    }

}
