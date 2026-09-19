package com.sgic.defect_tracker.specification;

import com.sgic.defect_tracker.dtos.request.DefectFilterDTO;
import com.sgic.defect_tracker.entities.Defect;
import com.sgic.defect_tracker.entities.Employee;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DefectSpecification {

    public static Specification<Defect> filter(DefectFilterDTO dto) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if(dto.getProjectId()!=null){
                predicates.add(
                        cb.equal(root.get("projectDetails")
                                .get("projectId"), dto.getProjectId())
                );
            }

            if(dto.getStatusTypeId()!=null){
                predicates.add(
                        cb.equal(root.get("statusType")
                                .get("statusTypeId"), dto.getStatusTypeId())
                );
            }

            if(dto.getSeverityId()!=null){
                predicates.add(
                        cb.equal(root.get("severity")
                                .get("severityId"), dto.getSeverityId())
                );
            }

            if(dto.getPriorityId()!=null){
                predicates.add(
                        cb.equal(root.get("priority")
                                .get("priorityId"), dto.getPriorityId())
                );
            }

            if(dto.getDefectTypeId()!=null){
                predicates.add(
                        cb.equal(root.get("defectType")
                                .get("defectTypeId"), dto.getDefectTypeId())
                );
            }

            if(dto.getModuleId()!=null){
                predicates.add(
                        cb.equal(root.get("module")
                                .get("moduleId"), dto.getModuleId())
                );
            }

            if(dto.getSubModuleId()!=null){
                predicates.add(
                        cb.equal(root.get("subModule")
                                .get("subModuleId"), dto.getSubModuleId())
                );
            }

//            if(dto.getSearch()!=null && !dto.getSearch().trim().isEmpty()){
//
//                String keyword="%"+dto.getSearch().toLowerCase()+"%";
//
//                Predicate description=
//                        cb.like(cb.lower(root.get("briefDescription")),keyword);
//
//                Predicate assign=
//                        cb.like(cb.lower(root.get("assignTo")),keyword);
//
//                Predicate enterBy=
//                        cb.like(cb.lower(root.get("enterBy")),keyword);
//
//                Predicate steps=
//                        cb.like(cb.lower(root.get("steps")),keyword);
//
//                Predicate project=
//                        cb.like(cb.lower(root.get("projectDetails")
//                                .get("projectName")),keyword);
//
//                Predicate module=
//                        cb.like(cb.lower(root.get("module")
//                                .get("moduleName")),keyword);
//
//                Predicate subModule=
//                        cb.like(cb.lower(root.get("subModule")
//                                .get("subModuleName")),keyword);
//
//                Predicate severity=
//                        cb.like(cb.lower(root.get("severity")
//                                .get("severityName")),keyword);
//
//                Predicate priority=
//                        cb.like(cb.lower(root.get("priority")
//                                .get("priorityName")),keyword);
//
//                Predicate status=
//                        cb.like(cb.lower(root.get("statusType")
//                                .get("statusName")),keyword);
//
//                Predicate defectType=
//                        cb.like(cb.lower(root.get("defectType")
//                                .get("defectTypeName")),keyword);
//
//                predicates.add(cb.or(
//                        description,
//                        assign,
//                        enterBy,
//                        steps,
//                        project,
//                        module,
//                        subModule,
//                        severity,
//                        priority,
//                        status,
//                        defectType
//                ));
//            }

//            if (dto.getSearch() != null && !dto.getSearch().trim().isEmpty()) {
//
//                String keyword = "%" + dto.getSearch().trim().toLowerCase() + "%";
//
//                // Defect fields
//                Predicate description =
//                        cb.like(
//                                cb.lower(root.get("briefDescription")),
//                                keyword
//                        );
//
//                Predicate steps =
//                        cb.like(
//                                cb.lower(root.get("steps")),
//                                keyword
//                        );
//
//                Predicate enterBy =
//                        cb.like(
//                                cb.lower(root.get("enterBy")),
//                                keyword
//                        );
//
//                // Assigned employee
//                Predicate assignFirstName =
//                        cb.like(
//                                cb.lower(root.get("assignTo").get("firstName")),
//                                keyword
//                        );
//
//                Predicate assignLastName =
//                        cb.like(
//                                cb.lower(root.get("assignTo").get("lastName")),
//                                keyword
//                        );
//
//                // Project
//                Predicate project =
//                        cb.like(
//                                cb.lower(root.get("projectDetails")
//                                        .get("projectName")),
//                                keyword
//                        );
//
//                // Module
//                Predicate module =
//                        cb.like(
//                                cb.lower(root.get("module")
//                                        .get("moduleName")),
//                                keyword
//                        );
//
//                // Sub Module
//                Predicate subModule =
//                        cb.like(
//                                cb.lower(root.get("subModule")
//                                        .get("subModuleName")),
//                                keyword
//                        );
//
//                // Severity
//                Predicate severity =
//                        cb.like(
//                                cb.lower(root.get("severity")
//                                        .get("severityName")),
//                                keyword
//                        );
//
//                // Priority
//                Predicate priority =
//                        cb.like(
//                                cb.lower(root.get("priority")
//                                        .get("priorityName")),
//                                keyword
//                        );
//
//                // Status
//                Predicate status =
//                        cb.like(
//                                cb.lower(root.get("statusType")
//                                        .get("statusName")),
//                                keyword
//                        );
//
//                // Defect Type
//                Predicate defectType =
//                        cb.like(
//                                cb.lower(root.get("defectType")
//                                        .get("defectTypeName")),
//                                keyword
//                        );
//
//                predicates.add(cb.or(
//                        description,
//                        steps,
//                        enterBy,
//                        assignFirstName,
//                        assignLastName,
//                        project,
//                        module,
//                        subModule,
//                        severity,
//                        priority,
//                        status,
//                        defectType
//                ));
//            }

//            if (dto.getSearch() != null && !dto.getSearch().trim().isEmpty()) {
//
//                String keyword = "%" + dto.getSearch().trim().toLowerCase() + "%";
//
//                Join<Defect, Employee> assignToJoin = root.join("assignTo", JoinType.LEFT);
//
//                Predicate description = cb.like(cb.lower(root.get("briefDescription")), keyword);
//                Predicate steps       = cb.like(cb.lower(root.get("steps")), keyword);
//
//                // enterBy is numeric — cast, don't lower() a raw numeric path
//                Predicate enterBy = cb.like(root.get("enterBy").as(String.class), keyword);
//
//                Predicate assignFirstName = cb.like(cb.lower(assignToJoin.get("firstName")), keyword);
//                Predicate assignLastName  = cb.like(cb.lower(assignToJoin.get("lastName")), keyword);
//
//                Predicate project    = cb.like(cb.lower(root.get("projectDetails").get("projectName")), keyword);
//                Predicate module      = cb.like(cb.lower(root.get("module").get("moduleName")), keyword);
//                Predicate subModule   = cb.like(cb.lower(root.get("subModule").get("subModuleName")), keyword);
//                Predicate severity    = cb.like(cb.lower(root.get("severity").get("severityName")), keyword);
//                Predicate priority    = cb.like(cb.lower(root.get("priority").get("priorityName")), keyword);
//                Predicate status      = cb.like(cb.lower(root.get("statusType").get("statusName")), keyword);
//                Predicate defectType  = cb.like(cb.lower(root.get("defectType").get("defectTypeName")), keyword);
//
//                predicates.add(cb.or(
//                        description
//
//                ));
//
//                query.distinct(true);
//            }
//            if (dto.getSearch() != null && !dto.getSearch().trim().isEmpty()) {
//
//                String keyword = "%" + dto.getSearch().trim().toLowerCase() + "%";
//
//                Join<Defect, Employee> assignToJoin = root.join("assignTo", JoinType.LEFT);
//
//                // Defect ID — cast numeric PK to string for partial match
//                Predicate defectIdMatch =
//                        cb.like(root.get("defectId").as(String.class), keyword);
//
//                Predicate description = cb.like(cb.lower(root.get("briefDescription")), keyword);
//                Predicate steps       = cb.like(cb.lower(root.get("steps")), keyword);
//                Predicate enterBy     = cb.like(root.get("enterBy").as(String.class), keyword);
//
//                Predicate assignFirstName = cb.like(cb.lower(assignToJoin.get("firstName")), keyword);
//                Predicate assignLastName  = cb.like(cb.lower(assignToJoin.get("lastName")), keyword);
//
//                Predicate project    = cb.like(cb.lower(root.get("projectDetails").get("projectName")), keyword);
//                Predicate module      = cb.like(cb.lower(root.get("module").get("moduleName")), keyword);
//                Predicate subModule   = cb.like(cb.lower(root.get("subModule").get("subModuleName")), keyword);
//                Predicate severity    = cb.like(cb.lower(root.get("severity").get("severityName")), keyword);
//                Predicate priority    = cb.like(cb.lower(root.get("priority").get("priorityName")), keyword);
//                Predicate status      = cb.like(cb.lower(root.get("statusType").get("statusName")), keyword);
//                Predicate defectType  = cb.like(cb.lower(root.get("defectType").get("defectTypeName")), keyword);
//
//                predicates.add(cb.or(
//                        defectIdMatch,
//                        description, steps, enterBy,
//                        assignFirstName, assignLastName,
//                        project, module, subModule,
//                        severity, priority, status, defectType
//                ));
//
//                query.distinct(true);
//            }
            if (dto.getSearch() != null && !dto.getSearch().trim().isEmpty()) {

                String keyword = "%" + dto.getSearch().trim().toLowerCase() + "%";

                Join<Defect, Employee> assignToJoin = root.join("assignTo", JoinType.LEFT);

                // Force a real CAST by routing through concat() — Hibernate's LIKE
                // predicate silently drops a bare .as(String.class) cast for numeric
                // columns, which caused: "operator does not exist: bigint ~~ text"
                Expression<String> defectIdAsString =
                        cb.concat(root.get("defectId").as(String.class), "");
                Predicate defectIdMatch = cb.like(defectIdAsString, keyword);

                Predicate description = cb.like(cb.lower(root.get("briefDescription")), keyword);
                Predicate steps       = cb.like(cb.lower(root.get("steps")), keyword);

                Expression<String> enterByAsString =
                        cb.concat(root.get("enterBy").as(String.class), "");
                Predicate enterBy = cb.like(enterByAsString, keyword);

                Predicate assignFirstName = cb.like(cb.lower(assignToJoin.get("firstName")), keyword);
                Predicate assignLastName  = cb.like(cb.lower(assignToJoin.get("lastName")), keyword);

                Predicate project    = cb.like(cb.lower(root.get("projectDetails").get("projectName")), keyword);
                Predicate module      = cb.like(cb.lower(root.get("module").get("moduleName")), keyword);
                Predicate subModule   = cb.like(cb.lower(root.get("subModule").get("subModuleName")), keyword);
                Predicate severity    = cb.like(cb.lower(root.get("severity").get("severityName")), keyword);
                Predicate priority    = cb.like(cb.lower(root.get("priority").get("priorityName")), keyword);
                Predicate status      = cb.like(cb.lower(root.get("statusType").get("statusName")), keyword);
                Predicate defectType  = cb.like(cb.lower(root.get("defectType").get("defectTypeName")), keyword);

                predicates.add(cb.or(
                        defectIdMatch,
                        description

                ));

                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
