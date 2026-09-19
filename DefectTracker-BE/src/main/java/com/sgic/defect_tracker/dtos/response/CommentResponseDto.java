package com.sgic.defect_tracker.dtos.response;

import lombok.Data;

import java.time.Instant;

@Data
public class CommentResponseDto{

    private Long id;

    private String comment;

//    private Instant createdAt;
//
//    private String createdBy;

    private String createdByName;
}