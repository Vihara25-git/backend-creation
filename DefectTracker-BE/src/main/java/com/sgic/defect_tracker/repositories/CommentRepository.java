package com.sgic.defect_tracker.repositories;

import com.sgic.defect_tracker.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    List<Comment> findByDefect_DefectId(Long defectId);

    void deleteByDefect_DefectId(Long defectId);

    Optional<Comment> findByCommentIdAndDefect_DefectId(
            Long commentId,
            Long defectId
    );
}