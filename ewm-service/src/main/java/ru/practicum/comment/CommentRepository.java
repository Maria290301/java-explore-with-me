package ru.practicum.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.comment.dto.CountCommentsByEventDto;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEvent_Id(Long eventId, Pageable pageable);

    List<Comment> findByAuthor_Id(Long userId);

    Optional<Comment> findByAuthor_IdAndId(Long userId, Long id);

    @Query("select new ru.practicum.comment.dto.CountCommentsByEventDto(c.event.id, COUNT(c)) " +
            "from Comment c where c.event.id in :eventIds " +
            "GROUP BY c.event.id")
    List<CountCommentsByEventDto> countCommentByEvent(List<Long> eventIds);

    @Query("select c from Comment c where lower(c.text) like lower(concat('%', :text, '%'))")
    List<Comment> search(@Param("text") String text, Pageable pageable);
}
