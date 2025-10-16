package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto createItem(Long userId, ItemCreateDto itemDto);

    ItemResponseDto updateItem(Long userId, Long itemId, ItemUpdateDto itemDto);

    ItemResponseDto getItem(Long userId, Long itemId);

    List<ItemResponseDto> getUserItems(Long userId, int from, int size);

    List<ItemResponseDto> searchItems(String text, int from, int size);
}