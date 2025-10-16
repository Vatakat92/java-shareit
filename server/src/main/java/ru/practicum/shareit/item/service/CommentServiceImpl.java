package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BusinessValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentCreateDto dto) {
        log.debug("Создание комментария для вещи: {} пользователем: {}", itemId, userId);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден: {}", userId);
                    return new EntityNotFoundException("Пользователь не найден: " + userId);
                });

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Вещь не найдена: {}", itemId);
                    return new EntityNotFoundException("Вещь не найдена: " + itemId);
                });

        boolean hasApprovedBooking = bookingRepository.existsCompletedBookingByUserAndItem(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now()
        );
        log.info("hasApprovedBooking for itemId={}, userId={}: {}", itemId, userId, hasApprovedBooking);

        if (!hasApprovedBooking) {
            log.debug("Пользователь {} не имеет APPROVED бронирования вещи {}", userId, itemId);
            throw new BusinessValidationException(String.format(
                    "Невозможно оставить отзыв на вещь %d: нет подтвержденного бронирования для пользователя %d", itemId, userId));
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        try {
            Comment saved = commentRepository.save(comment);
            log.info("Создан новый комментарий ID: {} к вещи ID: {} от пользователя ID: {}",
                    saved.getId(), itemId, userId);
            return CommentMapper.toDto(saved);
        } catch (Exception e) {
            log.error("Ошибка при сохранении комментария: {}", e.getMessage(), e);
            throw new RuntimeException("Произошла ошибка при сохранении комментария", e);
        }
    }
}