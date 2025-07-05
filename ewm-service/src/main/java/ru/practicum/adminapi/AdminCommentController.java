package ru.practicum.adminapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.Comment;
import ru.practicum.comment.CommentService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/admin/comments")
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("DELETE запрос на удаление комментария id = {} ", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }

    @GetMapping("/search")
    public List<Comment> searchComments(@RequestParam(name = "text") String text,
                                        @RequestParam(value = "from", defaultValue = "0") Integer from,
                                        @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("GET Запрос на поиск комментариев c текстом = {}", text);
        return commentService.search(text, from, size);
    }
}