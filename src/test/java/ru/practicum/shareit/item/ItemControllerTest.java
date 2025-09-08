package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean
    ItemService itemService;

    @Test
    @DisplayName("POST /items -> 201 Created + Location")
    void createItem_created201() throws Exception {
        Mockito.when(itemService.createItem(Mockito.any(ItemDto.class), Mockito.anyLong()))
                .thenReturn(new ItemDto(1L, "Drill", "Desc", true, null));

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 10)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Drill",
                                "Desc", true, null))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/items/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")));
    }

    @Test
    @DisplayName("POST /items missing header -> 400")
    void createItem_missingHeader400() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ItemDto(null, "Drill",
                                "Desc", true, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /items invalid body -> 400 (Bean Validation)")
    void createItem_invalid400() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 10)
                        .content(mapper.writeValueAsString(new ItemDto(null, "",
                                "", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /items/{id} forbidden -> 403")
    void updateItem_forbidden403() throws Exception {
        Mockito.when(itemService.updateItem(eq(5L), Mockito.any(ItemDto.class), eq(99L)))
                .thenThrow(new SecurityException("Only owner can update item"));

        mvc.perform(patch("/items/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 99)
                        .content(mapper.writeValueAsString(new ItemDto(null, "X",
                                null, null, null))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
    }

    @Test
    @DisplayName("GET /items/{id} not found -> 404")
    void getItem_notFound404() throws Exception {
        Mockito.when(itemService.getItemById(123L)).thenThrow(new NoSuchElementException("Item not found"));

        mvc.perform(get("/items/123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("GET /items? by owner -> 200")
    void getItemsByOwner_ok() throws Exception {
        Mockito.when(itemService.getAllItemsByOwner(77L)).thenReturn(List.of(
                new ItemDto(1L, "D", "d", true, null)
        ));

        mvc.perform(get("/items").header("X-Sharer-User-Id", 77))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }
}
