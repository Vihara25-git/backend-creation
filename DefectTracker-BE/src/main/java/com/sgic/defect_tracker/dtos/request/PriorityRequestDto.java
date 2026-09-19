package com.sgic.defect_tracker.dtos.request;


import lombok.Data;

@Data
public class PriorityRequestDto {
    private String priorityName;
    private String colorCode;
}
