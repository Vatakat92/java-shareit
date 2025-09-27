package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.Create;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Description is required")
    private String description;

    private Long requestorId;

    private LocalDateTime created;

    private List<ItemDto> items;
}