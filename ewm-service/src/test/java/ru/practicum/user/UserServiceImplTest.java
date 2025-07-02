package ru.practicum.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addNewUser_ShouldSaveAndReturnUserDto() {
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .build();

        User user = User.builder()
                .id(1L)
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        var result = userService.addNewUser(newUserRequest);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteWhenExists() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_ShouldThrowNotFoundException_WhenUserNotFound() {
        Long userId = 99L;
        when(userRepository.existsById(userId)).thenReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));

        assertEquals("Пользователь с id= " + userId + " не найден", ex.getMessage());

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getListUsers_ShouldReturnUsersByIds() {
        List<Long> ids = List.of(1L, 2L);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<User> users = List.of(
                User.builder().id(1L).name("John").email("john@example.com").build(),
                User.builder().id(2L).name("Jane").email("jane@example.com").build()
        );

        when(userRepository.findByIdIn(eq(ids), any(Pageable.class))).thenReturn(users);

        List<UserDto> result = userService.getListUsers(ids, 0, 10);

        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
        assertEquals("Jane", result.get(1).getName());

        verify(userRepository, times(1)).findByIdIn(eq(ids), any(Pageable.class));
    }

    @Test
    void getListUsers_ShouldReturnAllUsers_WhenIdsIsNull() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<User> users = List.of(
                User.builder().id(1L).name("John").email("john@example.com").build(),
                User.builder().id(2L).name("Jane").email("jane@example.com").build()
        );

        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(users));

        List<UserDto> result = userService.getListUsers(null, 0, 10);

        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
        assertEquals("Jane", result.get(1).getName());

        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }
}
