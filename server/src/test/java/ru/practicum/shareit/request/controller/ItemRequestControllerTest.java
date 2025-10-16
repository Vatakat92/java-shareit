package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void create_WhenValidData_ShouldReturnCreated() throws Exception {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("Need a drill");

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Need a drill");
        responseDto.setCreated(LocalDateTime.now());

        when(requestService.createRequest(anyLong(), any())).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void getAllByUser_WhenValidData_ShouldReturnOk() throws Exception {
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Test request");

        when(requestService.getUserRequests(anyLong())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Test request"));
    }

    @Test
    void getAllOtherUsersRequests_WhenValidData_ShouldReturnOk() throws Exception {
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Other user request");

        when(requestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Other user request"));
    }

    @Test
    void getAllOtherUsersRequests_WhenDefaultPagination_ShouldUseDefaults() throws Exception {
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);

        when(requestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getById_WhenValidData_ShouldReturnOk() throws Exception {
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Specific request");

        when(requestService.getRequest(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Specific request"));
    }
}