package ru.practicum.shareit.booking.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.exception.NotItemOwnerException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto createBooking(Long userId, BookingDto bookingDto) {
        log.debug("Creating booking for user: {}", userId);
        if (bookingDto == null) {
            log.error("BookingDto is null");
            throw new IllegalArgumentException("BookingDto cannot be null");
        }
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new EntityNotFoundException("User not found: " + userId);
                });
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> {
                    log.error("Item not found: {}", bookingDto.getItemId());
                    return new EntityNotFoundException("Item not found: " + bookingDto.getItemId());
                });
        if (!item.getAvailable()) {
            log.error("Item is not available: {}", bookingDto.getItemId());
            throw new ValidationException("Item is not available: " + bookingDto.getItemId());
        }
        if (item.getOwner().getId().equals(userId)) {
            log.error("User {} tried to book their own item {}", userId, bookingDto.getItemId());
            throw new EntityNotFoundException("Owner cannot book their own item: " + bookingDto.getItemId());
        }
        Booking booking = BookingMapper.toEntity(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        if (approved == null) {
            log.error("Approved status is null for booking: {}", bookingId);
            throw new IllegalArgumentException("Approved status cannot be null");
        }
        log.debug("{} booking: {} by user: {}", approved ? "Approving" : "Rejecting", bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new EntityNotFoundException("Booking not found: " + bookingId);
                });
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.error("User {} is not the owner of item in booking {}", userId, bookingId);
            throw new NotItemOwnerException("Only owner can approve booking: " + bookingId);
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            log.error("Booking {} already processed with status: {}", bookingId, booking.getStatus());
            throw new ValidationException("Booking already processed: " + bookingId);
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        log.debug("Getting booking: {} for user: {}", bookingId, userId);
        if (userId == null) {
            log.error("User ID is null");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new EntityNotFoundException("Booking not found: " + bookingId);
                });
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            log.error("User {} is not authorized to view booking {}", userId, bookingId);
            throw new NotItemOwnerException("Only booker or owner can view booking: " + bookingId);
        }
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        log.debug("Getting {} bookings for user: {}, from: {}, size: {}", state, userId, from, size);
        if (state == null) {
            log.error("State is null");
            throw new IllegalArgumentException("State cannot be null");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new EntityNotFoundException("User not found: " + userId);
                });

        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        return switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByBookerIdOrderByStartDateDesc(userId, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "CURRENT" -> bookingRepository.findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(
                            userId, now, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "PAST" -> bookingRepository.findByBookerIdAndEndDateBeforeOrderByStartDateDesc(
                            userId, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "FUTURE" -> bookingRepository.findByBookerIdAndStartDateAfterOrderByStartDateDesc(
                            userId, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "WAITING", "REJECTED" -> {
                BookingStatus status = BookingStatus.valueOf(state.toUpperCase());
                yield bookingRepository.findByBookerIdAndStatusOrderByStartDateDesc(
                                userId, status, page)
                        .stream().map(BookingMapper::toResponseDto).toList();
            }
            default -> throw new ValidationException("Unknown state: " + state);
        };
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, int from, int size) {
        log.debug("Getting {} bookings for owner: {}, from: {}, size: {}", state, userId, from, size);
        if (state == null) {
            log.error("State is null");
            throw new IllegalArgumentException("State cannot be null");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Owner not found: {}", userId);
                    return new EntityNotFoundException("User not found: " + userId);
                });

        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        return switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByItemOwnerIdOrderByStartDateDesc(userId, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "CURRENT" -> bookingRepository.findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(
                            userId, now, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "PAST" -> bookingRepository.findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(
                            userId, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "FUTURE" -> bookingRepository.findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(
                            userId, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "WAITING", "REJECTED" -> {
                BookingStatus status = BookingStatus.valueOf(state.toUpperCase());
                yield bookingRepository.findByItemOwnerIdAndStatusOrderByStartDateDesc(
                                userId, status, page)
                        .stream().map(BookingMapper::toResponseDto).toList();
            }
            default -> throw new ValidationException("Unknown state: " + state);
        };
    }
}