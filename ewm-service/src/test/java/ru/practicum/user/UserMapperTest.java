package ru.practicum.user;

import org.junit.jupiter.api.Test;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toEntity_shouldMapCorrectly() {
        NewUserRequest request = NewUserRequest.builder()
                .name("Test")
                .email("test@example.com")
                .build();

        User user = userMapper.toEntity(request);

        assertEquals("Test", user.getName());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void toDto_shouldMapCorrectly() {
        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("test@example.com")
                .build();

        UserDto dto = userMapper.toDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("Test", dto.getName());
        assertEquals("test@example.com", dto.getEmail());
    }
}
