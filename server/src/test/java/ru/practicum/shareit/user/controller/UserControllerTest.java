package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUserWhenValidData() throws Exception {
        UserCreateDto userDto = new UserCreateDto();
        userDto.setName("Test User");
        userDto.setEmail("test@mail.com");

        UserDto responseDto = new UserDto();
        responseDto.setId(1L);
        responseDto.setName("Test User");
        responseDto.setEmail("test@mail.com");

        when(userService.createUser(any(UserCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void getUserByIdWhenValidData() throws Exception {
        UserDto responseDto = new UserDto();
        responseDto.setId(1L);
        responseDto.setName("Test User");
        responseDto.setEmail("test@mail.com");

        when(userService.getUser(anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void getAllUsersWhenValidData() throws Exception {
        UserDto responseDto = new UserDto();
        responseDto.setId(1L);
        responseDto.setName("Test User");

        when(userService.getAllUsers()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test User"));
    }

    @Test
    void updateUserWhenValidData() throws Exception {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("Updated User");
        updateDto.setEmail("updated@mail.com");

        UserDto responseDto = new UserDto();
        responseDto.setId(1L);
        responseDto.setName("Updated User");
        responseDto.setEmail("updated@mail.com");

        when(userService.updateUser(anyLong(), any(UserUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.email").value("updated@mail.com"));
    }

    @Test
    void deleteUserByIdWhenValidData() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }
}