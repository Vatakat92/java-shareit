package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void saveNewUser_withValidData_shouldReturnOk() throws Exception {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("John");
        userCreateDto.setEmail("john@example.com");

        when(userClient.create(any(UserCreateDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isOk());

        verify(userClient, times(1)).create(any(UserCreateDto.class));
    }

    @Test
    void saveNewUser_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        UserCreateDto userCreateDto = new UserCreateDto();
        userCreateDto.setName("John");
        userCreateDto.setEmail("invalid-email"); // Invalid email

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).create(any(UserCreateDto.class));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        Long userId = 1L;
        when(userClient.getById(userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userClient, times(1)).getById(userId);
    }

    @Test
    void getAllUsers_shouldReturnUserList() throws Exception {
        when(userClient.getAll())
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userClient, times(1)).getAll();
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        Long userId = 1L;
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("Updated Name");
        userUpdateDto.setEmail("updated@example.com");

        when(userClient.update(eq(userId), any(UserUpdateDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk());

        verify(userClient, times(1)).update(eq(userId), any(UserUpdateDto.class));
    }

    @Test
    void deleteUserById_shouldCallDelete() throws Exception {
        Long userId = 1L;

        when(userClient.delete(userId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userClient, times(1)).delete(userId);
    }
}