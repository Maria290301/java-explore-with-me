package ru.practicum.user;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ru.practicum.privateapi.PrivateUserController;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserUpdateRequest;

@WebMvcTest(PrivateUserController.class)
class PrivateUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getCurrentUser_shouldReturnUserDto() throws Exception {
        UserDto dto = new UserDto(1L, "John", "john@example.com");
        when(userService.getUserById(1L)).thenReturn(dto);

        mockMvc.perform(get("/users/me")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void updateCurrentUser_shouldReturnUpdatedUserDto() throws Exception {
        UserUpdateRequest updateRequest = new UserUpdateRequest("New Name", "new@example.com");
        UserDto updatedDto = new UserDto(1L, "New Name", "new@example.com");

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/users/me")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }
}
