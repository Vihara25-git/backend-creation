package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.DefectFilterDTO;
import com.sgic.defect_tracker.dtos.request.DefectRequestDTO;
import com.sgic.defect_tracker.dtos.response.DefectByModuleResponseDTO;
import com.sgic.defect_tracker.dtos.response.DefectDailyCountResponseDTO;
import com.sgic.defect_tracker.dtos.response.DefectResponseDTO;
import com.sgic.defect_tracker.dtos.response.DefectSeverityBreakdownResponseDTO;
import com.sgic.defect_tracker.entities.Defect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import com.sgic.defect_tracker.dtos.response.ImportDefectResponseDTO;

import java.util.List;

public interface DefectService {

    DefectResponseDTO createdefect(DefectRequestDTO defectRequestDTO,MultipartFile attachmentImage);
    DefectResponseDTO updatedefect(Long id, DefectRequestDTO defectRequestDTO,MultipartFile attachmentImage
                                   );
    Page<DefectResponseDTO> GetDefectAll(Pageable pageable);

    Page<DefectResponseDTO> getProjectId_Defect(Long projectId,Pageable pageable);

    DefectResponseDTO getDefectById(Long defectId);

    void deleteDefect(Long id);

    List<DefectResponseDTO> getDefectByStatusTypeId(Long statusTypeId);

    List<DefectResponseDTO> getDefectByseverityId(Long severityId);

    List<DefectResponseDTO> getDefectBypriorityId(Long priorityId);

    List<DefectResponseDTO> getDefectBydefectTypeId(Long defectTypeId);
    List<DefectResponseDTO> filterDefects(DefectFilterDTO dto);

    List<DefectByModuleResponseDTO> getDefectsByModule(Long projectId);

    List<DefectDailyCountResponseDTO> getTimeToFindDefects(Long projectId, Long releaseId);

    List<DefectDailyCountResponseDTO> getTimeToFixDefects(Long projectId, Long releaseId);

    DefectSeverityBreakdownResponseDTO getDefectSeverityBreakdown(Long projectId);

    //bulk reassign
    void bulkReassignDefects(
            List<Long> defectIds,
            Long assignedToId
    );
    byte[] exportDefectsToExcel(Long projectId);

    //Import defects
    ImportDefectResponseDTO importDefects(
            MultipartFile file,
            Long projectId
    );


}
