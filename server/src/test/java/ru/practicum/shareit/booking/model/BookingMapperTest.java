package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    @Test
    void toResponseDto_ShouldMapCorrectly() {
        User booker = new User();
        booker.setId(1L);
        booker.setName("Booker");
        booker.setEmail("booker@mail.com");

        Item item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStartDate(LocalDateTime.now().plusDays(1));
        booking.setEndDate(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        var result = BookingMapper.toResponseDto(booking);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStartDate(), result.getStart());
        assertEquals(booking.getEndDate(), result.getEnd());
        assertEquals(booking.getStatus(), result.getStatus());
        assertNotNull(result.getItem());
        assertEquals(booking.getItem().getId(), result.getItem().getId());
        assertNotNull(result.getBooker());
        assertEquals(booking.getBooker().getId(), result.getBooker().getId());
    }

    @Test
    void toEntity_ShouldMapCorrectly() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        User booker = new User();
        booker.setId(1L);

        Item item = new Item();
        item.setId(1L);

        var result = BookingMapper.toEntity(bookingDto, item, booker);

        assertNotNull(result);
        assertEquals(bookingDto.getStart(), result.getStartDate());
        assertEquals(bookingDto.getEnd(), result.getEndDate());
        assertEquals(item, result.getItem());
        assertEquals(booker, result.getBooker());
    }

    @Test
    void toShortDto_ShouldMapCorrectly() {
        User booker = new User();
        booker.setId(1L);
        booker.setName("Booker");
        booker.setEmail("booker@mail.com");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStartDate(LocalDateTime.now().plusDays(1));
        booking.setEndDate(LocalDateTime.now().plusDays(2));
        booking.setBooker(booker);

        var result = BookingMapper.toShortDto(booking);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStartDate(), result.getStart());
        assertEquals(booking.getEndDate(), result.getEnd());
        assertEquals(booking.getBooker().getId(), result.getBookerId());
    }
}