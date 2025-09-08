package ru.practicum.shareit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShareItIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private Long userId;

    @BeforeEach
    void setup() throws Exception {
        var resp = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserDto(null, "Owner",
                                "owner-" + UUID.randomUUID() + "@test.com"))))
                .andExpect(status().isCreated())
                .andReturn();
        var created = mapper.readValue(resp.getResponse().getContentAsByteArray(), UserDto.class);
        userId = created.getId();
    }

    @Test
    @DisplayName("end-to-end: create user, conflict on duplicate email")
    void userCreate_and_conflict() throws Exception {
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserDto(null, "U", "dup@test.com"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/users/\\d+")));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserDto(null, "U2", "dup@test.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("end-to-end: PATCH user email -> 409 on duplicate")
    void userPatch_emailUnique_conflict409() throws Exception {
        var u1 = mapper.readValue(
                mvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(new UserDto(null, "A",
                                        "a-" + UUID.randomUUID() + "@t.com"))))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsByteArray(),
                UserDto.class);

        var u2 = mapper.readValue(
                mvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(new UserDto(null, "B", "b-" +
                                        UUID.randomUUID() + "@t.com"))))
                        .andExpect(status().isCreated())
                        .andReturn().getResponse().getContentAsByteArray(),
                UserDto.class);

        mvc.perform(patch("/users/" + u2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserDto(null, null, u1.getEmail()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    @DisplayName("end-to-end: item create, missing header 400, owner not found 404, forbidden patch 403, search")
    void item_flow_with_errors_and_search() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Drill",
                                "Desc", true, null))))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 9999)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Drill",
                                "Desc", true, null))))
                .andExpect(status().isNotFound());

        var create1 = mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Hammer",
                                "Strong hammer", true, null))))
                .andExpect(status().isCreated())
                .andReturn();
        var item1 = mapper.readValue(create1.getResponse().getContentAsByteArray(), ItemDto.class);

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(new ItemDto(null, "HAM Radio",
                                "Device", false, null))))
                .andExpect(status().isCreated());

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Screwdriver",
                                "small hammer", true, null))))
                .andExpect(status().isCreated());

        mvc.perform(patch("/items/" + item1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId + 1)
                        .content(mapper.writeValueAsString(new ItemDto(null, "X",
                                null, null, null))))
                .andExpect(status().isForbidden());

        mvc.perform(get("/items/search").param("text", "ham"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].available", everyItem(is(true))));
    }

    @Test
    @DisplayName("end-to-end: get items by owner -> list without pagination")
    void getItemsByOwner_listOk() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Alpha",
                                "a", true, null))))
                .andExpect(status().isCreated());

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Beta",
                                "b", true, null))))
                .andExpect(status().isCreated());

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Gamma",
                                "g", false, null))))
                .andExpect(status().isCreated());

        mvc.perform(get("/items").header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Alpha", "Beta", "Gamma")));
    }
}
