package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.CalculateKlocRequestDTO;
import com.sgic.defect_tracker.dtos.response.CalculateKlocResponseDto;

public interface GitHubKlocService {

//    CalculateKlocResponseDto calculateKloc(
//            CalculateKlocRequestDTO request
//    );

    CalculateKlocResponseDto calculateKloc(
            Long projectId,
            CalculateKlocRequestDTO request
    );


}