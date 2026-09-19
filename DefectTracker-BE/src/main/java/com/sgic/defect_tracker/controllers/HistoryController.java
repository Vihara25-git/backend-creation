package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.HistoryRequestDto;
import com.sgic.defect_tracker.dtos.response.HistoryResponseDto;
import com.sgic.defect_tracker.services.HistoryService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/defect")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @PostMapping("/history")
    public ResponseEntity<ResponseWrapper<HistoryResponseDto>> createHistory(
            @RequestBody HistoryRequestDto dto
    ) {

        HistoryResponseDto response =
                historyService.createHistory(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        new ResponseWrapper<>(
                                201,
                                "Defect history created successfully",
                                response
                        )
                );
    }

    @GetMapping("/{defectId}/history")
    public ResponseEntity<ResponseWrapper<List<HistoryResponseDto>>>
    getDefectHistory(
            @PathVariable Long defectId
    ) {

        List<HistoryResponseDto> response =
                historyService.getDefectHistory(defectId);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        200,
                        "Defect history retrieved successfully",
                        response
                )
        );
    }
}