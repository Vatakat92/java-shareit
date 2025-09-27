package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.practicum.shareit.validation.Create;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Comment text is required")
    @Size(max = 1000, message = "text must not exceed 1000 characters")
    private String text;

    private Long itemId;

    private Long authorId;
    private String authorName;

    private LocalDateTime created;
}