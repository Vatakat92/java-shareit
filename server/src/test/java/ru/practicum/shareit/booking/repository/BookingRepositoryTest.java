package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking currentBooking;
    private Booking pastBooking;
    private Booking futureBooking;
    private Booking waitingBooking;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@mail.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@mail.com");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        pastBooking = new Booking();
        pastBooking.setStartDate(LocalDateTime.now().minusDays(10));
        pastBooking.setEndDate(LocalDateTime.now().minusDays(5));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        pastBooking = bookingRepository.save(pastBooking);

        currentBooking = new Booking();
        currentBooking.setStartDate(LocalDateTime.now().minusDays(1));
        currentBooking.setEndDate(LocalDateTime.now().plusDays(1));
        currentBooking.setItem(item);
        currentBooking.setBooker(booker);
        currentBooking.setStatus(BookingStatus.APPROVED);
        currentBooking = bookingRepository.save(currentBooking);

        futureBooking = new Booking();
        futureBooking.setStartDate(LocalDateTime.now().plusDays(5));
        futureBooking.setEndDate(LocalDateTime.now().plusDays(10));
        futureBooking.setItem(item);
        futureBooking.setBooker(booker);
        futureBooking.setStatus(BookingStatus.APPROVED);
        futureBooking = bookingRepository.save(futureBooking);

        waitingBooking = new Booking();
        waitingBooking.setStartDate(LocalDateTime.now().plusDays(15));
        waitingBooking.setEndDate(LocalDateTime.now().plusDays(20));
        waitingBooking.setItem(item);
        waitingBooking.setBooker(booker);
        waitingBooking.setStatus(BookingStatus.WAITING);
        waitingBooking = bookingRepository.save(waitingBooking);
    }

    @Test
    void findByBookerIdOrderByStartDateDesc_ShouldReturnBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());

        var result = bookingRepository.findByBookerIdOrderByStartDateDesc(booker.getId(), pageable);

        assertNotNull(result);
        assertEquals(4, result.getContent().size());
        assertTrue(result.getContent().get(0).getStartDate().isAfter(result.getContent().get(1).getStartDate()));
    }

    @Test
    void findByBookerIdAndStatusOrderByStartDateDesc_ShouldReturnFilteredBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());

        var result = bookingRepository.findByBookerIdAndStatusOrderByStartDateDesc(
                booker.getId(), BookingStatus.WAITING, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(BookingStatus.WAITING, result.getContent().getFirst().getStatus());
    }

    @Test
    void findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc_ShouldReturnCurrentBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());

        var result = bookingRepository.findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(
                booker.getId(), LocalDateTime.now(), LocalDateTime.now(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(currentBooking.getId(), result.getContent().getFirst().getId());
    }

    @Test
    void findByBookerIdAndEndDateBeforeOrderByStartDateDesc_ShouldReturnPastBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());

        var result = bookingRepository.findByBookerIdAndEndDateBeforeOrderByStartDateDesc(
                booker.getId(), LocalDateTime.now(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(pastBooking.getId(), result.getContent().getFirst().getId());
    }

    @Test
    void findByBookerIdAndStartDateAfterOrderByStartDateDesc_ShouldReturnFutureBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());

        var result = bookingRepository.findByBookerIdAndStartDateAfterOrderByStartDateDesc(
                booker.getId(), LocalDateTime.now(), pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(b -> b.getStartDate().isAfter(LocalDateTime.now())));
    }

    @Test
    void findByItemOwnerIdOrderByStartDateDesc_ShouldReturnOwnerBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());

        var result = bookingRepository.findByItemOwnerIdOrderByStartDateDesc(owner.getId(), pageable);

        assertNotNull(result);
        assertEquals(4, result.getContent().size());
        assertTrue(result.getContent().get(0).getStartDate().isAfter(result.getContent().get(1).getStartDate()));
    }

    @Test
    void findByItemOwnerIdAndStatusOrderByStartDateDesc_ShouldReturnFilteredOwnerBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());

        var result = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDateDesc(
                owner.getId(), BookingStatus.APPROVED, pageable);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(b -> b.getStatus() == BookingStatus.APPROVED));
    }

    @Test
    void findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc_ShouldReturnCurrentOwnerBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());

        var result = bookingRepository.findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(
                owner.getId(), LocalDateTime.now(), LocalDateTime.now(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(currentBooking.getId(), result.getContent().getFirst().getId());
    }

    @Test
    void findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc_ShouldReturnPastOwnerBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());

        var result = bookingRepository.findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(
                owner.getId(), LocalDateTime.now(), pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(pastBooking.getId(), result.getContent().getFirst().getId());
    }

    @Test
    void findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc_ShouldReturnFutureOwnerBookings() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startDate").descending());

        var result = bookingRepository.findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(
                owner.getId(), LocalDateTime.now(), pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(b -> b.getStartDate().isAfter(LocalDateTime.now())));
    }

    @Test
    void findLastBooking_WhenExists_ShouldReturnLastBooking() {
        bookingRepository.delete(currentBooking);

        Optional<Booking> result = bookingRepository.findLastBooking(
                item.getId(), LocalDateTime.now());

        assertTrue(result.isPresent());
        assertEquals(pastBooking.getId(), result.get().getId());
    }

    @Test
    void findNextBooking_ShouldReturnNextBooking() {
        Optional<Booking> result = bookingRepository.findNextBooking(
                item.getId(), LocalDateTime.now());

        assertTrue(result.isPresent());
        assertEquals(futureBooking.getId(), result.get().getId());
    }

    @Test
    void findNextBooking_WhenNoFutureBookings_ShouldReturnEmpty() {
        bookingRepository.deleteAll();
        Booking past = new Booking();
        past.setStartDate(LocalDateTime.now().minusDays(10));
        past.setEndDate(LocalDateTime.now().minusDays(5));
        past.setItem(item);
        past.setBooker(booker);
        past.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(past);


        Optional<Booking> result = bookingRepository.findNextBooking(
                item.getId(), LocalDateTime.now());

        assertFalse(result.isPresent());
    }

    @Test
    void findNextBooking_ShouldIgnoreWaitingStatus() {
        Optional<Booking> result = bookingRepository.findNextBooking(
                item.getId(), LocalDateTime.now());

        assertTrue(result.isPresent());
        assertEquals(futureBooking.getId(), result.get().getId());
        assertNotEquals(waitingBooking.getId(), result.get().getId());
    }

    @Test
    void pagination_ShouldWorkCorrectly() {
        Pageable firstPage = PageRequest.of(0, 2, Sort.by("startDate").descending());

        var result = bookingRepository.findByBookerIdOrderByStartDateDesc(booker.getId(), firstPage);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }
}