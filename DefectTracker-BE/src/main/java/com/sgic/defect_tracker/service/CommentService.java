package com.sgic.defect_tracker.service;

import com.sgic.defect_tracker.dtos.request.CommentRequestDto;
import com.sgic.defect_tracker.dtos.response.CommentResponseDto;

import java.util.List;

public interface CommentService {
 List<CommentResponseDto> getCommentsByDefectId(Long defectId) ;


CommentResponseDto addComment(Long defectId, CommentRequestDto commentRequestDTO) ;


 Object updateComment(Long defectId, Long commentId, CommentRequestDto commentRequestDTO) ;
void deleteComment(Long defectId, Long commentId) ;
}
