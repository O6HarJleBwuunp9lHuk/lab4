package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.CreateUserRequest;
import org.example.dto.UpdateUserRequest;
import org.example.dto.UserDto;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com", 30);
        UserDto response = new UserDto(1L, "John Doe", "john@example.com", 30);

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.age").value(30));
    }

    @Test
    void getAllUsers_ShouldReturnUserList() throws Exception {
        // Given
        List<UserDto> users = Arrays.asList(
            new UserDto(1L, "John Doe", "john@example.com", 30),
            new UserDto(2L, "Jane Smith", "jane@example.com", 25)
        );

        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].name").value("John Doe"))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        // Given
        UserDto user = new UserDto(1L, "John Doe", "john@example.com", 30);

        when(userService.getUserById(1L)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.age").value(30));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("John Updated");
        request.setAge(35);

        UserDto response = new UserDto(1L, "John Updated", "john@example.com", 35);

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John Updated"))
            .andExpect(jsonPath("$.age").value(35));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateUserRequest invalidRequest = new CreateUserRequest("", "invalid-email", -5);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void checkUserExists_ShouldReturnTrue() throws Exception {
        // Given
        when(userService.userExists("existing@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/users/exists")
                .param("email", "existing@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
