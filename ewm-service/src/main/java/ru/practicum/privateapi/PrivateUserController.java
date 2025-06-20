package ru.practicum.privateapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.UserService;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserUpdateRequest;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/users/me")
public class PrivateUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("X-User-Id") Long userId,
                                                  HttpServletRequest request) {
        log.info("GET /users/me — запрос от пользователя с ID: {}, IP: {}, путь: {}",
                userId, request.getRemoteAddr(), request.getRequestURI());

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PatchMapping
    public ResponseEntity<UserDto> updateCurrentUser(@RequestHeader("X-User-Id") Long userId,
                                                     @RequestBody @Validated UserUpdateRequest updateRequest,
                                                     HttpServletRequest request) {
        log.info("PATCH /users/me — обновление пользователя с ID: {}, тело: {}, IP: {}, путь: {}",
                userId, updateRequest, request.getRemoteAddr(), request.getRequestURI());

        return ResponseEntity.ok(userService.updateUser(userId, updateRequest));
    }
}