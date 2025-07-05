package ru.practicum.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.event.dto.EventStatus;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.UncorrectedParametersException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto patchByUser(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        User user = checkUser(userId);

        Comment comment = checkComment(commentId);

        checkAuthorComment(user, comment);

        if (comment.getCreated().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new UncorrectedParametersException("Сообщение возможно отредактировать только в течение часа");
        }

        comment.setText(updateCommentDto.getText());
        comment.setLastUpdatedOn(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(updatedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentUser(Long userId) {
        checkUser(userId);
        List<Comment> commentList = commentRepository.findByAuthor_Id(userId);
        return commentList.stream().map(CommentMapper::toCommentDto).collect(Collectors.toList());
    }

    public Comment getUserCommentByUserAndCommentId(Long userId, Long commentId) {
        checkUser(userId);
        return commentRepository.findByAuthor_IdAndId(userId, commentId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Комментарий с id=%d для пользователя id=%d не найден", commentId, userId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentEvent(Long eventId, Integer from, Integer size) {
        Event event = checkEvent(eventId);
        PageRequest pageable = PageRequest.of(from / size, size);
        return commentRepository.findAllByEvent_Id(eventId, pageable);

    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        User user = checkUser(userId);
        Comment comment = checkComment(commentId);
        checkAuthorComment(user, comment);
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = checkComment(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public List<Comment> search(String text, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return commentRepository.search(text, pageable);
    }

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto commentDto) {
        Event event = checkEvent(eventId);
        User user = checkUser(userId);
        if (!event.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new UncorrectedParametersException("Невозможно добавить комментарий к событию со статусом не PUBLISHED");
        }
        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDto, event, user)));
    }

    private Event checkEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("Событие с id=%d  не найдено", id)));
    }

    private User checkUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("Пользователь c id=%d  не найден", id)));
    }

    private Comment checkComment(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("Комментарий c id=%d  не найден", id)));
    }

    private void checkAuthorComment(User user, Comment comment) {
        if (!comment.getAuthor().equals(user)) {
            throw new UncorrectedParametersException("Пользователь не является автором комментария");
        }
    }
}