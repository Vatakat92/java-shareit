package ru.practicum.shareit.booking.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
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
import ru.practicum.shareit.exception.SecurityException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto createBooking(Long userId, BookingDto bookingDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item not found: " + bookingDto.getItemId()));
        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available: " + bookingDto.getItemId());
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new EntityNotFoundException("Owner cannot book their own item: " + bookingDto.getItemId());
        }
        Booking booking = BookingMapper.toEntity(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingId));
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new SecurityException("Only owner can approve booking: " + bookingId);
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking already processed: " + bookingId);
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingId));
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new SecurityException("Only booker or owner can view booking: " + bookingId);
        }
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        return switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByBookerIdOrderByStartDesc(userId, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "CURRENT" -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                            userId, now, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "PAST" -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                            userId, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "FUTURE" -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                            userId, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "WAITING", "REJECTED" -> {
                BookingStatus status = BookingStatus.valueOf(state.toUpperCase());
                yield bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                                userId, status, page)
                        .stream().map(BookingMapper::toResponseDto).toList();
            }
            default -> throw new ValidationException("Unknown state: " + state);
        };
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        return switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByOwnerId(userId, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "CURRENT" -> bookingRepository.findCurrentByOwnerId(userId, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "PAST" -> bookingRepository.findPastByOwnerId(userId, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "FUTURE" -> bookingRepository.findFutureByOwnerId(userId, now, page)
                    .stream().map(BookingMapper::toResponseDto).toList();

            case "WAITING", "REJECTED" -> {
                BookingStatus status = BookingStatus.valueOf(state.toUpperCase());
                yield bookingRepository.findByOwnerIdAndStatus(userId, status, page)
                        .stream().map(BookingMapper::toResponseDto).toList();
            }
            default -> throw new ValidationException("Unknown state: " + state);
        };
    }
}