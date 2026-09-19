package com.sgic.defect_tracker.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration
public class FlyWayConfig {

    @Bean
    public Flyway flyway(
            DataSource dataSource,
            @Value("${spring.flyway.locations}") String locations,
            @Value("${spring.flyway.baseline-on-migrate}") boolean baselineOnMigrate,
            @Value("${spring.flyway.baseline-version}") String baselineVersion) {

        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion(baselineVersion)
                .load();
    }

    @Bean
    @DependsOn("entityManagerFactory")
    public FlywayMigrationRunner flywayMigrationRunner(Flyway flyway) {
        return new FlywayMigrationRunner(flyway);
    }

    public static class FlywayMigrationRunner {

        public FlywayMigrationRunner(Flyway flyway) {
            flyway.migrate();
        }
    }
}

//package com.sgic.defect_tracker.config;
//import org.flywaydb.core.Flyway;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//
//@Component
//public class FlyWayConfig {
//
//    private final DataSource dataSource;
//
//    public FlyWayConfig(DataSource dataSource) {
//        this.dataSource = dataSource;
//    }
//
//    @EventListener(ApplicationReadyEvent.class)
//    public void runFlywayMigration() {
//
//        Flyway flyway = Flyway.configure()
//                .dataSource(dataSource)
//                .load();
//
//        flyway.migrate();
//    }
//}
//
