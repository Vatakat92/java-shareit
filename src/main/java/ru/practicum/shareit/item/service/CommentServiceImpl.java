package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto dto) {
        log.debug("Создание комментария для вещи: {} пользователем: {}", itemId, userId);

        if (dto == null) {
            log.error("Передан пустой DTO комментария");
            throw new IllegalArgumentException("Комментарий не может быть пустым");
        }
        if (dto.getText() == null || dto.getText().isBlank()) {
            log.error("Текст комментария пустой");
            throw new IllegalArgumentException("Текст комментария не может быть пустым");
        }

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

        bookingRepository.findPastApprovedForComment(itemId, userId, LocalDateTime.now())
                .orElseThrow(() -> {
                    log.warn("Пользователь {} не бронировал вещь {}", userId, itemId);
                    return new IllegalArgumentException("Вы не можете оставить отзыв, так как не бронировали эту вещь");
                });

        Comment comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        try {
            comment = commentRepository.save(comment);
            log.info("Создан новый комментарий ID: {} к вещи ID: {} от пользователя ID: {}",
                    comment.getId(), itemId, userId);
            return CommentMapper.toDto(comment);
        } catch (Exception e) {
            log.error("Ошибка при сохранении комментария: {}", e.getMessage(), e);
            throw new RuntimeException("Произошла ошибка при сохранении комментария", e);
        }
    }
}
