package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemResponseDto createItem(Long userId, ItemCreateDto itemDto) {
        log.debug("Создание вещи для пользователя: {}, itemDto: {}", userId, itemDto);
        if (itemDto == null) {
            log.error("ItemDto is null");
            throw new IllegalArgumentException("ItemDto cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден: {}", userId);
                    return new EntityNotFoundException("Пользователь не найден: " + userId);
                });

        Item item = ItemMapper.toEntity(itemDto, user);

        if (itemDto.getRequestId() != null) {
            item.setRequest(requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> {
                        log.error("Запрос не найден: {}", itemDto.getRequestId());
                        return new EntityNotFoundException("Запрос не найден: " + itemDto.getRequestId());
                    }));
        }

        Item savedItem = itemRepository.save(item);
        log.info("Создана вещь с ID: {}, available: {}, ownerId: {}", savedItem.getId(), savedItem.getAvailable(), savedItem.getOwner().getId());
        return ItemMapper.toItemResponseDto(savedItem, List.of());
    }

    @Override
    public ItemResponseDto updateItem(Long userId, Long itemId, ItemUpdateDto itemDto) {
        log.debug("Обновление вещи: {} для пользователя: {}", itemId, userId);
        if (itemDto == null) {
            log.error("ItemDto is null");
            throw new IllegalArgumentException("ItemDto cannot be null");
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Вещь не найдена: {}", itemId);
                    return new EntityNotFoundException("Вещь не найдена: " + itemId);
                });
        if (!item.getOwner().getId().equals(userId)) {
            log.error("Пользователь {} не является владельцем вещи {}", userId, itemId);
            throw new NotItemOwnerException("Только владелец может обновить вещь: " + itemId);
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        Item updatedItem = itemRepository.save(item);
        log.info("Вещь обновлена: id={}, available={}", updatedItem.getId(), updatedItem.getAvailable());
        return ItemMapper.toItemResponseDto(updatedItem, List.of());
    }

    @Override
    public ItemResponseDto getItem(Long itemId, Long userId) {
        log.debug("Получение вещи: {} для пользователя: {}", itemId, userId);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден: {}", userId);
                    return new EntityNotFoundException("Пользователь не найден: " + userId);
                });
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Вещь не найдена: {}", itemId);
                    return new EntityNotFoundException("Вещь не найдена: " + itemId);
                });
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
        ItemResponseDto dto = ItemMapper.toItemResponseDto(item, comments);
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            dto.setLastBooking(bookingRepository.findLastBooking(itemId, now)
                    .map(BookingMapper::toShortDto).orElse(null));
            dto.setNextBooking(bookingRepository.findNextBooking(itemId, now)
                    .map(BookingMapper::toShortDto).orElse(null));
        }
        return dto;
    }

    @Override
    public List<ItemResponseDto> getUserItems(Long userId, int from, int size) {
        log.debug("Получение вещей для пользователя: {}, from: {}, size: {}", userId, from, size);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден: {}", userId);
                    return new EntityNotFoundException("Пользователь не найден: " + userId);
                });
        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return itemRepository.findByOwnerId(userId, page).stream()
                .map(item -> {
                    List<CommentDto> comments = commentRepository.findByItemId(item.getId()).stream()
                            .map(CommentMapper::toDto)
                            .collect(Collectors.toList());
                    ItemResponseDto dto = ItemMapper.toItemResponseDto(item, comments);
                    if (item.getOwner().getId().equals(userId)) {
                        dto.setLastBooking(bookingRepository.findLastBooking(item.getId(), now)
                                .map(BookingMapper::toShortDto).orElse(null));
                        dto.setNextBooking(bookingRepository.findNextBooking(item.getId(), now)
                                .map(BookingMapper::toShortDto).orElse(null));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemResponseDto> searchItems(String text, int from, int size) {
        log.debug("Поиск вещей с текстом: '{}', from: {}, size: {}", text, from, size);
        if (text == null || text.isBlank()) {
            log.info("Текст поиска пустой, возвращается пустой список");
            return List.of();
        }
        PageRequest page = PageRequest.of(from / size, size);
        return itemRepository.search(text, page).stream()
                .map(item -> ItemMapper.toItemResponseDto(item, List.of()))
                .collect(Collectors.toList());
    }
}