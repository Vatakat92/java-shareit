package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;
import ru.practicum.shareit.user.dto.UserShortDto;

@Data
public class BookingResponseDto {
    private Long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private ItemShortDto item;

    private UserShortDto booker;

    private BookingStatus status;
}