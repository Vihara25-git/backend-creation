package com.sgic.defect_tracker.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequestDto {

    @NotBlank(message = "Comment cannot be empty")
    private String comment;
}