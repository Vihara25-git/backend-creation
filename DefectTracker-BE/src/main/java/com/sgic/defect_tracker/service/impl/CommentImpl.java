package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.CommentRequestDto;
import com.sgic.defect_tracker.dtos.response.CommentResponseDto;
import com.sgic.defect_tracker.entities.Comment;
import com.sgic.defect_tracker.entities.Defect;
import com.sgic.defect_tracker.mapper.CommentMapper;
import com.sgic.defect_tracker.repositories.CommentRepository;
import com.sgic.defect_tracker.repositories.DefectRepository;
import com.sgic.defect_tracker.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final DefectRepository defectRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentResponseDto addComment(
            Long defectId,
            CommentRequestDto requestDto
    ) {

        Defect defect = defectRepository.findById(defectId)
                .orElseThrow(() ->
                        new RuntimeException("Defect not found")
                );

        Comment comment = commentMapper.toEntity(requestDto);

        comment.setDefect(defect);

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment);
    }

    @Override
    public List<CommentResponseDto> getCommentsByDefectId(
            Long defectId
    ) {

        List<Comment> comments =
                commentRepository.findByDefect_DefectId(defectId);

        return comments.stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    public CommentResponseDto updateComment(
            Long defectId,
            Long commentId,
            CommentRequestDto requestDto
    ) {

        Comment comment = commentRepository
                .findByCommentIdAndDefect_DefectId(
                        commentId,
                        defectId
                )
                .orElseThrow(() ->
                        new RuntimeException("Comment not found")
                );

        comment.setComment(requestDto.getComment());

        Comment updatedComment =
                commentRepository.save(comment);

        return commentMapper.toDto(updatedComment);
    }

    @Override
    public void deleteComment(
            Long defectId,
            Long commentId
    ) {

        Comment comment = commentRepository
                .findByCommentIdAndDefect_DefectId(
                        commentId,
                        defectId
                )
                .orElseThrow(() ->
                        new RuntimeException("Comment not found")
                );

        commentRepository.delete(comment);
    }
}