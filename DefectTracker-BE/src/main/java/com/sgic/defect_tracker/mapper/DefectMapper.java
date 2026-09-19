package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.DefectRequestDTO;
import com.sgic.defect_tracker.dtos.response.DefectResponseDTO;
import com.sgic.defect_tracker.entities.Defect;
import com.sgic.defect_tracker.entities.ReleaseView;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DefectMapper {

    default Long mapReleaseId(List<ReleaseView> releases) {
        if(releases != null && !releases.isEmpty()) {
            return releases.get(0).getReleaseId();
        }
        return null;
    }

    default String mapReleaseName(List<ReleaseView> releases) {
        if(releases != null && !releases.isEmpty()) {
            return releases.get(0).getReleaseName();
        }
        return null;
    }

    @Mapping(source = "assignTo.empId",target = "assignToId")
    @Mapping(source = "assignTo.firstName",target="assignToName")
    @Mapping(source = "releaseViews", target = "releaseId")
    @Mapping(source = "releaseViews", target = "releaseName")

    @Mapping(source = "module.moduleId", target = "moduleId")
    @Mapping(source = "module.moduleName", target = "moduleName")

    @Mapping(source = "subModule.subModuleId", target = "subModuleId")
    @Mapping(source = "subModule.subModuleName", target = "subModuleName")

    @Mapping(source = "defectType.defectTypeId", target = "defectTypeId")
    @Mapping(source = "defectType.defectTypeName", target = "defectTypeName")

    @Mapping(source = "severity.severityId", target = "severityId")
    @Mapping(source = "severity.severityName", target = "severityName")

    @Mapping(source = "priority.priorityId", target = "priorityId")
    @Mapping(source = "priority.priorityName", target = "priorityName")

    @Mapping(source = "statusType.statusTypeId", target = "statusTypeId")
    @Mapping(source = "statusType.statusName", target = "statusName")

    @Mapping(source = "projectDetails.projectId", target = "projectId")
    @Mapping(source = "projectDetails.projectName", target = "projectName")
    @Mapping(source = "testCase.testCaseId", target = "testCaseId")
    DefectResponseDTO toResponse(Defect defect);





//
//    @Mapping(target = "module", ignore = true)
//    @Mapping(target = "subModule", ignore = true)
//    @Mapping(target = "defectType", ignore = true)
//    @Mapping(target = "severity", ignore = true)
//    @Mapping(target = "priority", ignore = true)
//    @Mapping(target = "statusType", ignore = true)
//    @Mapping(target = "projectDetails", ignore = true)
//    @Mapping(target = "releaseViews", ignore = true)
@Mapping(
        target = "briefDescription",
        expression = "java(defectRequestDTO.getBriefDescription() == null ? null : defectRequestDTO.getBriefDescription().trim().replaceAll(\"\\\\s+\", \" \"))"
)
    @Mapping(target = "subDev", ignore = true)
    Defect toEntity(DefectRequestDTO defectRequestDTO);



    @Mapping(
            target = "briefDescription",
            expression = "java(dto.getBriefDescription() == null ? null : dto.getBriefDescription().trim().replaceAll(\"\\\\s+\", \" \"))"
    )
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatedefctfromdto(
            DefectRequestDTO dto,
            @MappingTarget Defect defect
    );

}