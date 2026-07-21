package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

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
    }

    @Test
    void createBooking_IntegrationTest() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        var result = bookingService.createBooking(booker.getId(), bookingDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());

        var savedBooking = bookingRepository.findById(result.getId());
        assertTrue(savedBooking.isPresent());
        assertEquals(result.getId(), savedBooking.get().getId());
    }

    @Test
    void getUserBookings_IntegrationTest() {
        BookingCreateDto bookingDto1 = new BookingCreateDto();
        bookingDto1.setItemId(item.getId());
        bookingDto1.setStart(LocalDateTime.now().plusDays(1));
        bookingDto1.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.createBooking(booker.getId(), bookingDto1);

        BookingCreateDto bookingDto2 = new BookingCreateDto();
        bookingDto2.setItemId(item.getId());
        bookingDto2.setStart(LocalDateTime.now().plusDays(3));
        bookingDto2.setEnd(LocalDateTime.now().plusDays(4));
        bookingService.createBooking(booker.getId(), bookingDto2);

        var result = bookingService.getUserBookings(booker.getId(), "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void approveBooking_IntegrationTest() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        var createdBooking = bookingService.createBooking(booker.getId(), bookingDto);

        var savedBooking = bookingRepository.findById(createdBooking.getId());
        assertTrue(savedBooking.isPresent());
        assertEquals(BookingStatus.WAITING, savedBooking.get().getStatus());

        var result = bookingService.approveBooking(owner.getId(), createdBooking.getId(), true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }
}