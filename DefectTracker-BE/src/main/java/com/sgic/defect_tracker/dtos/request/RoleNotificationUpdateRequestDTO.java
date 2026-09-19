package com.sgic.defect_tracker.dtos.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RoleNotificationUpdateRequestDTO {

    private Long roleId;

    private List<Long> pointSetupIds;

    private Map<Long, String> pointChannels;
}