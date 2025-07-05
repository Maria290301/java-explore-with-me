package ru.practicum.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.event.Event;
import ru.practicum.user.User;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {
    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .created(comment.getCreated())
                .lastUpdatedOn(comment.getLastUpdatedOn())
                .build();
    }

    public Comment toComment(NewCommentDto commentDto, Event event, User user) {
        return Comment.builder()
                .text(commentDto.getText())
                .event(event)
                .author(user)
                .created(LocalDateTime.now())
                .lastUpdatedOn(null)
                .build();
    }

    public Comment toComment(CommentDto commentDto) {
        return Comment.builder()
                .id(commentDto.getId())
                .text(commentDto.getText())
                .build();
    }
}
