package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateDto {

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 1, max = 2000, message = "Description must be between 1 and 2000 characters")
    private String description;

    @NotNull(message = "Available status is required")
    private Boolean available;

    @Positive(message = "Request ID must be positive")
    private Long requestId;
}