package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BusinessValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
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

        booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStartDate(LocalDateTime.now().minusDays(2));
        booking.setEndDate(LocalDateTime.now().minusDays(1));
        booking = bookingRepository.save(booking);
    }

    @Test
    void createComment_WhenValidData_IntegrationTest() {
        CommentCreateDto commentDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        CommentDto result = commentService.createComment(booker.getId(), item.getId(), commentDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Great item!", result.getText());
        assertEquals(item.getId(), result.getItemId());
        assertEquals(booker.getId(), result.getAuthorId());
        assertEquals(booker.getName(), result.getAuthorName());
        assertNotNull(result.getCreated());

        var savedComment = commentRepository.findById(result.getId());
        assertTrue(savedComment.isPresent());
        assertEquals("Great item!", savedComment.get().getText());
        assertEquals(item.getId(), savedComment.get().getItem().getId());
        assertEquals(booker.getId(), savedComment.get().getAuthor().getId());
    }

    @Test
    void createComment_WhenUserNotFound_IntegrationTest() {
        CommentCreateDto commentDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        assertThrows(EntityNotFoundException.class,
                () -> commentService.createComment(999L, item.getId(), commentDto));
    }

    @Test
    void createComment_WhenItemNotFound_IntegrationTest() {
        CommentCreateDto commentDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        assertThrows(EntityNotFoundException.class,
                () -> commentService.createComment(booker.getId(), 999L, commentDto));
    }

    @Test
    void createComment_WhenNoApprovedBooking_IntegrationTest() {
        User newBooker = new User();
        newBooker.setName("New Booker");
        newBooker.setEmail("newbooker@mail.com");
        newBooker = userRepository.save(newBooker);

        CommentCreateDto commentDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        User finalNewBooker = newBooker;
        assertThrows(BusinessValidationException.class,
                () -> commentService.createComment(finalNewBooker.getId(), item.getId(), commentDto));
    }
}