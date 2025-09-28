package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserShortDto;

public class BookingMapper {

    public static BookingResponseDto toResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        Item item = booking.getItem();
        ItemShortDto itemDto = null;
        if (item != null) {
            itemDto = ItemShortDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                    .build();
        }

        User booker = booking.getBooker();
        UserShortDto bookerDto = null;
        if (booker != null) {
            bookerDto = UserShortDto.builder()
                    .id(booker.getId())
                    .name(booker.getName())
                    .build();
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .item(itemDto)
                .booker(bookerDto)
                .status(booking.getStatus())
                .build();
    }

    public static BookingShortDto toShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker() != null ? booking.getBooker().getId() : null)
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .build();
    }

    public static Booking toEntity(BookingDto dto, Item item, User booker) {
        if (dto == null) {
            return null;
        }
        return Booking.builder()
                .startDate(dto.getStart())
                .endDate(dto.getEnd())
                .item(item)
                .booker(booker)
                .build();
    }
}
