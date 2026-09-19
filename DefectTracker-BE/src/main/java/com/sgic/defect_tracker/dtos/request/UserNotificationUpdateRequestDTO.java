package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

import java.util.Map;

@Data
public class UserNotificationUpdateRequestDTO {
    private Long userId;
    private Map<Long, String> pointChannels;
}