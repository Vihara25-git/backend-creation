package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.CalculateKlocRequestDTO;
import com.sgic.defect_tracker.dtos.response.CalculateKlocResponseDto;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.GitHubKlocService;
import com.sgic.defect_tracker.utils.EndpointBundle;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EndpointBundle.PROJECT)
public class GitHubKlocController {

    @Autowired
    private GitHubKlocService gitHubKlocService;

    @PostMapping("/{id}/calculate-kloc")
    public ResponseEntity<ResponseWrapper<CalculateKlocResponseDto>> calculateKloc(
            @PathVariable Long id,
            @Valid @RequestBody CalculateKlocRequestDTO request) {

        CalculateKlocResponseDto result =
                gitHubKlocService.calculateKloc(id, request);

        return ResponseEntity.ok(
                new ResponseWrapper<>(
                        RestApiResponseStatusCodes.SUCCESS.getCode(),
                        "KLOC calculated successfully",
                        result
                )
        );
    }
}