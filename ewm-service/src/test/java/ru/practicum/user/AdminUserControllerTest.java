package ru.practicum.user;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.adminapi.AdminUserController;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void create_shouldReturnCreatedUser() throws Exception {
        NewUserRequest request = new NewUserRequest("John", "john@example.com");
        UserDto dto = new UserDto(1L, "John", "john@example.com");

        when(userService.createUser(any())).thenReturn(dto);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getUsers_shouldReturnUserList() throws Exception {
        UserDto dto = new UserDto(1L, "John", "john@example.com");
        when(userService.getUsers(null, 0, 10)).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }
}
