package com.sgic.defect_tracker.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalculateKlocRequestDTO {

    private String backendRepo;

    private String frontendRepo;

    private String githubUsername;

    private String githubToken;
}