package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import java.util.Collections;

public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest request) {
        if (request == null) {
            return null;
        }
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setRequestorId(request.getRequestor() != null ? request.getRequestor().getId() : null);
        dto.setCreated(request.getCreated());
        dto.setItems(Collections.emptyList()); // Items добавляются в сервисе
        return dto;
    }

    public static ItemRequest toEntity(ItemRequestDto dto, User requestor) {
        if (dto == null) {
            return null;
        }
        ItemRequest request = new ItemRequest();
        request.setId(dto.getId());
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(dto.getCreated());
        return request;
    }
}