package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserShortDto;

public class BookingMapper {
    public static BookingResponseDto toResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        Item item = booking.getItem();
        if (item != null) {
            ItemShortDto itemDto = new ItemShortDto();
            itemDto.setId(item.getId());
            itemDto.setName(item.getName());
            itemDto.setDescription(item.getDescription());
            itemDto.setAvailable(item.getAvailable());
            if (item.getRequest() != null) {
                itemDto.setRequestId(item.getRequest().getId());
            } else {
                itemDto.setRequestId(null);
            }
            dto.setItem(itemDto);
        }

        User booker = booking.getBooker();
        if (booker != null) {
            UserShortDto bookerDto = new UserShortDto();
            bookerDto.setId(booker.getId());
            bookerDto.setName(booker.getName());
            dto.setBooker(bookerDto);
        }

        dto.setStatus(booking.getStatus());
        return dto;
    }

    public static BookingShortDto toShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        return dto;
    }

    public static Booking toEntity(BookingDto dto, Item item, User booker) {
        if (dto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        return booking;
    }
}