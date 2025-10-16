package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BusinessValidationException;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User createTestUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item createTestItem(Long id, String name, String description, boolean available, User owner) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        return item;
    }

    private Booking createTestBooking(Long id, LocalDateTime start, LocalDateTime end, Item item, User booker,
                                      BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }

    @Test
    void createBooking_WhenValidData_ShouldReturnBooking() {
        User testUser = createTestUser(1L, "Test User", "test@mail.com");
        User testOwner = createTestUser(2L, "Test Owner", "owner@mail.com");
        Item testItem = createTestItem(1L, "Test Item", "Test Description", true, testOwner);
        Booking testBooking = createTestBooking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), testItem, testUser, BookingStatus.WAITING);

        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(BookingStatus.WAITING);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(testItem));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        var result = bookingService.createBooking(1L, bookingDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenUserIsOwner_ShouldThrowException() {
        User testUser = createTestUser(1L, "Test User", "test@mail.com");
        Item testItem = createTestItem(1L, "Test Item", "Test Description", true, testUser);

        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(testItem));

        assertThrows(BusinessValidationException.class, () -> bookingService.createBooking(1L, bookingDto));
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowException() {
        User testUser = createTestUser(1L, "Test User", "test@mail.com");
        User testOwner = createTestUser(2L, "Test Owner", "owner@mail.com");
        Item testItem = createTestItem(1L, "Test Item", "Test Description", false,
                testOwner);

        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(testItem));

        assertThrows(BusinessValidationException.class, () -> bookingService.createBooking(1L, bookingDto));
    }

    @Test
    void createBooking_WhenStartAfterEnd_ShouldThrowException() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(BusinessValidationException.class, () -> bookingService.createBooking(1L, bookingDto));
    }

    @Test
    void approveBooking_WhenValidData_ShouldUpdateStatus() {
        User testUser = createTestUser(1L, "Test User", "test@mail.com");
        User testOwner = createTestUser(2L, "Test Owner", "owner@mail.com");
        Item testItem = createTestItem(1L, "Test Item", "Test Description", true,
                testOwner);
        Booking testBooking = createTestBooking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), testItem, testUser, BookingStatus.WAITING);

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        var result = bookingService.approveBooking(2L, 1L, true);

        verify(bookingRepository, times(1)).save(any(Booking.class));
        assertEquals(BookingStatus.APPROVED, testBooking.getStatus());
        assertEquals(1L, result.getId());
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void approveBooking_WhenNotOwner_ShouldThrowException() {
        User testUser = createTestUser(1L, "Test User", "test@mail.com");
        User testOwner = createTestUser(2L, "Test Owner", "owner@mail.com");
        Item testItem = createTestItem(1L, "Test Item", "Test Description", true,
                testOwner);
        Booking testBooking = createTestBooking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), testItem, testUser, BookingStatus.WAITING);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        assertThrows(NotItemOwnerException.class, () -> bookingService.approveBooking(3L, 1L, true));
    }

    @Test
    void getBooking_WhenValidUser_ShouldReturnBooking() {
        User testUser = createTestUser(1L, "Test User", "test@mail.com");
        User testOwner = createTestUser(2L, "Test Owner", "owner@mail.com");
        Item testItem = createTestItem(1L, "Test Item", "Test Description", true, testOwner);
        Booking testBooking = createTestBooking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), testItem, testUser, BookingStatus.WAITING);

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        var result = bookingService.getBooking(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getUserBookings_WhenValidState_ShouldReturnBookings() {
        User testUser = createTestUser(1L, "Test User", "test@mail.com");
        User testOwner = createTestUser(2L, "Test Owner", "owner@mail.com");
        Item testItem = createTestItem(1L, "Test Item", "Test Description", true,
                testOwner);
        Booking testBooking = createTestBooking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), testItem, testUser, BookingStatus.WAITING);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findByBookerIdOrderByStartDateDesc(anyLong(), any(Pageable.class)))
                .thenReturn(bookingPage);

        var result = bookingService.getUserBookings(1L, "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserBookings_WhenInvalidState_ShouldThrowException() {
        User testUser = createTestUser(1L, "Test User", "test@mail.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> bookingService.getUserBookings(1L,
                "INVALID", 0, 10));
    }

    @Test
    void getOwnerBookings_WhenValidState_ShouldReturnBookings() {
        User testOwner = createTestUser(1L, "Test Owner", "owner@mail.com");
        User testUser = createTestUser(2L, "Test User", "user@mail.com");
        Item testItem = createTestItem(1L, "Test Item", "Test Description",
                true, testOwner);
        Booking testBooking = createTestBooking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), testItem, testUser, BookingStatus.WAITING);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(List.of(testBooking));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDateDesc(anyLong(),
                any(Pageable.class))).thenReturn(bookingPage);

        var result = bookingService.getOwnerBookings(1L, "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}