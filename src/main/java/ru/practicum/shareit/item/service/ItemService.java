package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemResponseDto getItem(Long userId, Long itemId);

    List<ItemResponseDto> getUserItems(Long userId, int from, int size);

    List<ItemDto> searchItems(String text, int from, int size);
}