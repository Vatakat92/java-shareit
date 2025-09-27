package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemController Tests")
class ItemControllerTest {
    @Mock
    private ItemService itemService;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private ItemController itemController;

    private MockMvc mvc;
    private ObjectMapper mapper;
    private ItemDto itemDto;
    private ItemResponseDto itemResponseDto;
    private CommentDto commentDto;
    private Validator validator;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Set up validator
        validator = new LocalValidatorFactoryBean();
        ((LocalValidatorFactoryBean) validator).afterPropertiesSet();

        mvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .setValidator(validator)
                .build();

        // Initialize test data
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        itemResponseDto = new ItemResponseDto();
        itemResponseDto.setId(1L);
        itemResponseDto.setName("Test Item");
        itemResponseDto.setDescription("Test Description");
        itemResponseDto.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Test Comment");
        commentDto.setAuthorName("Test User");
        commentDto.setCreated(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create item and return 201 status")
    void createItem_ShouldReturnCreatedItem() throws Exception {
        when(itemService.createItem(anyLong(), ArgumentMatchers.any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/items/1")))
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())));

        verify(itemService).createItem(eq(1L), ArgumentMatchers.any(ItemDto.class));
    }

    @Test
    @DisplayName("Should update item and return 200 status")
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), ArgumentMatchers.any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())));

        verify(itemService).updateItem(eq(1L), eq(1L), ArgumentMatchers.any(ItemDto.class));
    }

    @Test
    @DisplayName("Should return item by id with 200 status")
    void getItem_ShouldReturnItem() throws Exception {
        when(itemService.getItem(anyLong(), anyLong())).thenReturn(itemResponseDto);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemResponseDto.getName())));

        verify(itemService).getItem(1L, 1L);
    }

    @Test
    @DisplayName("Should return user items with pagination")
    void getUserItems_ShouldReturnPaginatedItems() throws Exception {
        when(itemService.getUserItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemResponseDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemResponseDto.getId()), Long.class));

        verify(itemService).getUserItems(eq(1L), eq(0), eq(10));
    }

    @Test
    @DisplayName("Should search items by text with pagination")
    void searchItems_ShouldReturnMatchingItems() throws Exception {
        when(itemService.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .param("text", "test")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class));

        verify(itemService).searchItems(eq("test"), eq(0), eq(10));
    }

    @Test
    @DisplayName("Should create comment and return 201 status")
    void createComment_ShouldReturnCreatedComment() throws Exception {
        when(commentService.createComment(anyLong(), anyLong(), ArgumentMatchers.any(CommentDto.class)))
                .thenReturn(commentDto);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/items/1/comments/1")))
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.created").isNotEmpty());

        verify(commentService).createComment(eq(1L), eq(1L), ArgumentMatchers.any(CommentDto.class));
    }

    @Test
    @DisplayName("Should return 400 when creating item with invalid data")
    void createItem_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        ItemDto invalidItem = new ItemDto();
        invalidItem.setName(""); // Empty name is invalid
        invalidItem.setDescription("");
        invalidItem.setAvailable(null);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(invalidItem))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemService);
    }

    @Test
    @DisplayName("Should return 400 when updating item with invalid data")
    void updateItem_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        // Создаем невалидный JSON с пустым именем
        String invalidItemJson = "{\"name\":\"\", \"description\":\"Test\", \"available\":true}";

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(invalidItemJson)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").doesNotExist()); // Проверяем, что нет тела с ошибкой

        verifyNoInteractions(itemService);
    }

    @Test
    @DisplayName("Should return 400 when creating comment with invalid data")
    void createComment_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        CommentDto invalidComment = new CommentDto();
        invalidComment.setText(""); // Empty text is invalid

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(invalidComment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("Should return 400 when updating item with empty name")
    void updateItem_WhenEmptyName_ShouldReturnBadRequest() throws Exception {
        // Создаем JSON с пустым именем
        String invalidItemJson = "{\"name\":\"\"}";

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(invalidItemJson)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemService);
    }

    @Test
    @DisplayName("Should update only name when partial update")
    void updateItem_WhenPartialUpdate_ShouldUpdateOnlyProvidedFields() throws Exception {
        // Подготовка данных для обновления - только имя
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");

        // Ожидаемый результат - обновленное имя, остальные поля как в оригинале
        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(1L);
        expectedDto.setName("Updated Name");
        expectedDto.setDescription("Test Description");
        expectedDto.setAvailable(true);

        when(itemService.updateItem(anyLong(), anyLong(), ArgumentMatchers.any(ItemDto.class))).thenReturn(expectedDto);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(updateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(expectedDto.getName())))
                .andExpect(jsonPath("$.description", is(expectedDto.getDescription())))
                .andExpect(jsonPath("$.available", is(expectedDto.getAvailable())));

        verify(itemService).updateItem(eq(1L), eq(1L), ArgumentMatchers.any(ItemDto.class));
    }
}
