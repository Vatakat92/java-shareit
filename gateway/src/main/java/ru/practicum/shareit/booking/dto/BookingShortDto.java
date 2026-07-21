package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingShortDto {

    private Long id;

    private Long bookerId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in present or future")
    private LocalDateTime start;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in future")
    private LocalDateTime end;
}