package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.HistoryRequestDto;
import com.sgic.defect_tracker.dtos.response.HistoryResponseDto;
import com.sgic.defect_tracker.entities.Defect;
import com.sgic.defect_tracker.entities.DefectHistory;
import com.sgic.defect_tracker.entities.Employee;
import com.sgic.defect_tracker.mapper.HistoryMapper;
import com.sgic.defect_tracker.repositories.DefectHistoryRepository;
import com.sgic.defect_tracker.repositories.DefectRepository;
import com.sgic.defect_tracker.repositories.EmployeeRepository;
import com.sgic.defect_tracker.services.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryImpl implements HistoryService {

    private final DefectHistoryRepository defectHistoryRepository;
    private final DefectRepository defectRepository;
    private final EmployeeRepository employeeRepository;
    private final HistoryMapper historyMapper;

    @Override
    @Transactional
    public HistoryResponseDto createHistory(HistoryRequestDto dto) {

        Defect defect = defectRepository.findById(dto.getDefectId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Defect not found with id: " + dto.getDefectId()
                        )
                );

        Employee assignedBy = null;
        if (dto.getAssignedById() != null) {
            assignedBy = employeeRepository.findById(dto.getAssignedById())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Assigned By employee not found"
                            )
                    );
        }

        Employee assignedTo = null;
        if (dto.getAssignedToId() != null) {
            assignedTo = employeeRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Assigned To employee not found"
                            )
                    );
        }

        Employee updatedBy = null;
        if (dto.getUpdatedById() != null) {
            updatedBy = employeeRepository.findById(dto.getUpdatedById())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Updated By employee not found"
                            )
                    );
        }

        DefectHistory history = new DefectHistory();

        history.setDefect(defect);
        history.setAssignedBy(assignedBy);
        history.setAssignedTo(assignedTo);
        history.setPreviousStatus(dto.getPreviousStatus());
        history.setDefectStatus(dto.getDefectStatus());
        history.setName(dto.getName());
        history.setUpdatedBy(updatedBy);

        if (defect.getReleaseViews() != null
                && !defect.getReleaseViews().isEmpty()) {
            history.setReleaseName(
                    defect.getReleaseViews().getFirst().getReleaseName()
            );
        }

        history.setDefectDate(LocalDate.now());
        history.setDefectTime(LocalTime.now());

        DefectHistory savedHistory =
                defectHistoryRepository.save(history);

        return historyMapper.toDefectHistoryDto(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryResponseDto> getDefectHistory(Long defectId) {

        defectRepository.findById(defectId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Defect not found with id: " + defectId
                        )
                );

        List<DefectHistory> historyList =
                defectHistoryRepository
                        .findByDefect_DefectIdOrderByDefectDateDescDefectTimeDesc(
                                defectId
                        );

        return historyList.stream()
                .map(historyMapper::toDefectHistoryDto)
                .toList();
    }
}