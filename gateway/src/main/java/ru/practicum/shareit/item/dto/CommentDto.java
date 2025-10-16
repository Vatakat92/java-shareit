package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    private Long id;

    @NotNull(message = "Item ID is required")
    @Positive(message = "Item ID must be positive")
    private Long itemId;

    @NotBlank(message = "Comment text cannot be blank")
    @Size(min = 1, max = 1000, message = "Comment text must be between 1 and 1000 characters")
    private String text;

    private String authorName;

    private LocalDateTime created;
}