package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.Create;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Name cannot be empty")
    private String name;

    @NotBlank(groups = Create.class, message = "Description cannot be empty")
    private String description;

    @NotNull(groups = Create.class, message = "Available status is required")
    private Boolean available;

    private Long requestId;
}