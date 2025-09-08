package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean
    UserService userService;

    @Test
    @DisplayName("POST /users -> 201 Created + Location")
    void createUser_created201() throws Exception {
        Mockito.when(userService.createUser(any())).thenReturn(
                new UserDto(1L, "Alice", "alice@mail.com"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new UserDto(null, "Alice", "alice@mail.com"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("alice@mail.com")));
    }

    @Test
    @DisplayName("POST /users invalid -> 400 Bad Request (Bean Validation)")
    void createUser_invalid400() throws Exception {
        var body = new UserDto(null, "", "not-an-email");
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users duplicate email -> 409 Conflict")
    void createUser_conflict409() throws Exception {
        Mockito.when(userService.createUser(any())).thenThrow(new ConflictException("Email must be unique"));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserDto(null, "Bob", "dup@mail.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("unique")));
    }
}
