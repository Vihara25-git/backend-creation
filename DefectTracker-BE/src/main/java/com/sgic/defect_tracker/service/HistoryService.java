package com.sgic.defect_tracker.services;

import com.sgic.defect_tracker.dtos.request.HistoryRequestDto;
import com.sgic.defect_tracker.dtos.response.HistoryResponseDto;

import java.util.List;

public interface HistoryService {

    HistoryResponseDto createHistory(HistoryRequestDto dto);

    List<HistoryResponseDto> getDefectHistory(Long defectId);
}