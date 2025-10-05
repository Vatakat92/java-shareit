package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.Create;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    @NotNull(groups = Create.class, message = "Item ID is required")
    private Long itemId;

    @NotNull(groups = Create.class, message = "Start date is required")
    @FutureOrPresent(groups = Create.class, message = "Start date must be in the present or future")
    private LocalDateTime start;

    @NotNull(groups = Create.class, message = "End date is required")
    @Future(groups = Create.class, message = "End date must be in the future")
    private LocalDateTime end;
}