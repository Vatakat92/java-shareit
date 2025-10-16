package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestInternalDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapper {

    public static ItemRequestResponseDto toResponseDto(ItemRequest request) {
        if (request == null) {
            return null;
        }
        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(List.of()) // Явно устанавливаем пустой список
                .build();
    }

    public static ItemRequest toEntity(ItemRequestInternalDto requestDto, User user) {
        if (requestDto == null) {
            return null;
        }
        return ItemRequest.builder()
                .id(requestDto.getId())
                .description(requestDto.getDescription())
                .requestor(user)
                .created(requestDto.getCreated() != null ? requestDto.getCreated() : LocalDateTime.now())
                .build();
    }
}