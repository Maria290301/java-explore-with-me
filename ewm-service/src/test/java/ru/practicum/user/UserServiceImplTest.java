package ru.practicum.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserUpdateRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private NewUserRequest newUserRequest;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setup() {
        newUserRequest = NewUserRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();
    }

    @Test
    void createUser_success() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(newUserRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.createUser(newUserRequest);

        assertEquals(userDto, result);
    }

    @Test
    void createUser_emailAlreadyExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
                () -> userService.createUser(newUserRequest));

        assertEquals("Email already exists.", ex.getMessage());
    }

    @Test
    void getUsers_withIds() {
        List<Long> ids = List.of(1L);
        when(userRepository.findAllById(ids)).thenReturn(List.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.getUsers(ids, 0, 10);

        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
    }

    @Test
    void getUsers_withoutIds() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(userMapper.toDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.getUsers(null, 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void deleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertEquals(userDto, result);
    }

    @Test
    void getUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.getUserById(1L));

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void updateUser_success() {
        UserUpdateRequest updateRequest = new UserUpdateRequest("Updated Name", "updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.updateUser(1L, updateRequest);

        assertEquals(userDto, result);
        assertEquals("Updated Name", user.getName());
        assertEquals("updated@example.com", user.getEmail());
    }

    @Test
    void updateUser_partialUpdate() {
        UserUpdateRequest updateRequest = new UserUpdateRequest(null, "new@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        userService.updateUser(1L, updateRequest);

        assertEquals("new@email.com", user.getEmail());
        assertEquals("John Doe", user.getName());
    }

    @Test
    void updateUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.updateUser(1L, new UserUpdateRequest("x", "y")));
    }
}
