package com.sgic.defect_tracker.specification;

import com.sgic.defect_tracker.dtos.request.BenchAvailabilityViewFilterDTO;
import com.sgic.defect_tracker.entities.BenchAvailabilityView;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BenchAvailabilityViewSpecification {

    public static Specification<BenchAvailabilityView> filter(
            BenchAvailabilityViewFilterDTO dto) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Search by employee name
            if (dto.getSearch() != null &&
                    !dto.getSearch().trim().isEmpty()) {

                String keyword =
                        "%" + dto.getSearch().trim().toLowerCase() + "%";

                predicates.add(
                        cb.like(
                                cb.lower(root.get("employeeName")),
                                keyword
                        )
                );
            }

            // Filter by designation
            if (dto.getDesignationName() != null &&
                    !dto.getDesignationName().trim().isEmpty()) {

                predicates.add(
                        cb.equal(
                                root.get("designationName"),
                                dto.getDesignationName()
                        )
                );
            }

            // Filter by available percentage
            if (dto.getAvailablePercentage() != null) {

                predicates.add(
                        cb.equal(
                                root.get("availablePercentage"),
                                dto.getAvailablePercentage()
                        )
                );
            }

            // Available date >= start date
            if (dto.getStartDate() != null) {

                LocalDateTime startDate =
                        dto.getStartDate().atStartOfDay();

                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("availablePeriod"),
                                startDate
                        )
                );
            }

            // Available date <= end date
            if (dto.getEndDate() != null) {

                LocalDateTime endDate =
                        dto.getEndDate().atTime(23, 59, 59);

                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("availablePeriod"),
                                endDate
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}