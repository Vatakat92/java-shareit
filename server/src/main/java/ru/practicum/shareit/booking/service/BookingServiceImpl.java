package ru.practicum.shareit.booking.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
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
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingCreateDto bookingDto) {
        log.info("Создание бронирования для пользователя: {}", userId);

        // Упрощенные проверки как в работающем коде
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new BusinessValidationException("Дата окончания должна быть позже даты старта");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + userId));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Вещь не найдена: " + bookingDto.getItemId()));

        // Основные бизнес-правила
        if (item.getOwner().getId().equals(userId)) {
            throw new BusinessValidationException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new BusinessValidationException("Вещь недоступна для бронирования");
        }

        Booking booking = BookingMapper.toEntity(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        log.info("Создано бронирование: id={}, itemId={}, bookerId={}",
                saved.getId(), item.getId(), booker.getId());

        return BookingMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        log.info("Подтверждение бронирования: bookingId={}, userId={}, approved={}", bookingId, userId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено: " + bookingId));

        // Только одна проверка - как в работающем коде
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotItemOwnerException("Только владелец вещи может подтвердить бронирование");
        }

        // Упрощенная проверка статуса
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BusinessValidationException("Статус бронирования уже изменен");
        }

        // Устанавливаем статус
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updated = bookingRepository.save(booking);
        log.info("Бронирование обновлено: id={}, статус={}", bookingId, updated.getStatus());

        return BookingMapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        log.debug("Получение бронирования: {} для пользователя: {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено: " + bookingId));

        // Проверка доступа
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new EntityNotFoundException("Пользователь не имеет доступа к бронированию");
        }

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        log.debug("Бронирования пользователя: {}, состояние: {}", userId, state);

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + userId));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<Booking> bookings;
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByBookerIdOrderByStartDateDesc(userId, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(
                        userId, now, now, pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndDateBeforeOrderByStartDateDesc(
                        userId, now, pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartDateAfterOrderByStartDateDesc(
                        userId, now, pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDateDesc(
                        userId, BookingStatus.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDateDesc(
                        userId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state, int from, int size) {
        log.debug("Бронирования владельца: {}, состояние: {}", userId, state);

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + userId));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<Booking> bookings;
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDateDesc(userId, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(
                        userId, now, now, pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(
                        userId, now, pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(
                        userId, now, pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDateDesc(
                        userId, BookingStatus.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDateDesc(
                        userId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}