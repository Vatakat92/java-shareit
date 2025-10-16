package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void add_shouldReturnCreated() throws Exception {
        ItemCreateDto itemDto = createValidItemCreateDto();
        when(itemClient.create(anyLong(), any(ItemCreateDto.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.CREATED));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void add_shouldReturnBadRequestWhenInvalidItem() throws Exception {
        ItemCreateDto invalidItemDto = new ItemCreateDto();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setName("Updated Name");
        when(itemClient.update(anyLong(), anyLong(), any(ItemUpdateDto.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        when(itemClient.getItem(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void get_shouldReturnOk() throws Exception {
        when(itemClient.getUserItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void search_shouldReturnOk() throws Exception {
        when(itemClient.searchItems(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "test")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_shouldReturnCreated() throws Exception {
        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setText("Great item!");
        when(itemClient.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.CREATED));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void addComment_shouldReturnBadRequestWhenInvalidComment() throws Exception {
        CommentCreateDto invalidCommentDto = new CommentCreateDto(); // Missing text

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommentDto)))
                .andExpect(status().isBadRequest());
    }

    private ItemCreateDto createValidItemCreateDto() {
        ItemCreateDto itemDto = new ItemCreateDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        return itemDto;
    }
}