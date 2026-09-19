package com.sgic.defect_tracker.mapper;

import com.sgic.defect_tracker.dtos.request.CommentRequestDto;
import com.sgic.defect_tracker.dtos.response.CommentResponseDto;
import com.sgic.defect_tracker.entities.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "commentId", target = "id")
//    @Mapping(source = "createdAt", target = "createdAt")
//    @Mapping(source = "createdBy", target = "createdBy")
//    @Mapping(source = "createdBy", target = "createdByName")
    CommentResponseDto toDto(Comment comment);

    @Mapping(target = "commentId", ignore = true)
    @Mapping(target = "defect", ignore = true)
    Comment toEntity(CommentRequestDto commentRequestDto);
}