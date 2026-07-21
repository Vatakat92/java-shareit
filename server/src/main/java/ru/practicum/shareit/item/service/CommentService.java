package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;

public interface CommentService {
    CommentDto createComment(Long userId, Long itemId, CommentCreateDto commentDto);
}