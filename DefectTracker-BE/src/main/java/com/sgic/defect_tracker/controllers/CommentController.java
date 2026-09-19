package com.sgic.defect_tracker.controllers;

import com.sgic.defect_tracker.dtos.request.CommentRequestDto;
import com.sgic.defect_tracker.dtos.response.CommentResponseDto;
import com.sgic.defect_tracker.enums.RestApiResponseStatusCodes;
import com.sgic.defect_tracker.service.CommentService;
import com.sgic.defect_tracker.utils.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Service
@RestController
@RequestMapping("/api/v1/defect")
@RequiredArgsConstructor
public class CommentController {

    @Autowired
    private  CommentService commentService;

    // Create comment
    @PostMapping("/{defectId}/comment")
    public ResponseWrapper<CommentResponseDto> addComment(
            @PathVariable Long defectId,
            @RequestBody CommentRequestDto commentRequestDTO) {

        return new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS.getCode(),
                RestApiResponseStatusCodes.SUCCESS.getMessage(),
                commentService.addComment(defectId, commentRequestDTO)
        );
    }

    // Get comments
    @GetMapping("/{defectId}/comment")
    public ResponseWrapper<List<CommentResponseDto>> getComments(
            @PathVariable Long defectId) {

        return new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS.getCode(),
                RestApiResponseStatusCodes.SUCCESS.getMessage(),
                commentService.getCommentsByDefectId(defectId)
        );
    }

    // Update comment
    @PutMapping("/{defectId}/comment/{commentId}")
    public ResponseWrapper<Object> updateComment(
            @PathVariable Long defectId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto commentRequestDTO) {

        return new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS.getCode(),
                RestApiResponseStatusCodes.SUCCESS.getMessage(),
                commentService.updateComment(
                        defectId,
                        commentId,
                        commentRequestDTO
                )
        );
    }

    // Delete comment
    @DeleteMapping("/{defectId}/comment/{commentId}")
    public ResponseWrapper<Void> deleteComment(
            @PathVariable Long defectId,
            @PathVariable Long commentId) {

        commentService.deleteComment(defectId, commentId);

        return new ResponseWrapper<>(
                RestApiResponseStatusCodes.SUCCESS.getCode(),
                RestApiResponseStatusCodes.SUCCESS.getMessage(),
                null
        );
    }
}