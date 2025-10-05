package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotItemOwnerException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.CommentDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.debug("Creating item for user: {}", userId);
        if (itemDto == null) {
            log.error("ItemDto is null");
            throw new IllegalArgumentException("ItemDto cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new EntityNotFoundException("User not found: " + userId);
                });
        Item item = ItemMapper.toEntity(itemDto, user);
        if (itemDto.getRequestId() != null) {
            item.setRequest(requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new EntityNotFoundException("Request not found: " + itemDto.getRequestId())));
        }
        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.debug("Updating item: {} for user: {}", itemId, userId);
        if (itemDto == null) {
            log.error("ItemDto is null");
            throw new IllegalArgumentException("ItemDto cannot be null");
        }
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item not found: {}", itemId);
                    return new EntityNotFoundException("Item not found: " + itemId);
                });
        if (!item.getOwner().getId().equals(userId)) {
            log.error("User {} is not the owner of item {}", userId, itemId);
            throw new NotItemOwnerException("Only owner can update item: " + itemId);
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
        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemResponseDto getItem(Long itemId, Long userId) {
        log.debug("Getting item: {} for user: {}", itemId, userId);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new EntityNotFoundException("User not found: " + userId);
                });
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found: " + itemId));
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
        ItemResponseDto dto = ItemMapper.toItemResponseDto(item, comments);
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            dto.setLastBooking(bookingRepository.findLastBooking(itemId, now)
                    .map(BookingMapper::toShortDto).orElse(null));
            dto.setNextBooking(bookingRepository.findNextBooking(itemId, now)
                    .map(BookingMapper::toShortDto).orElse(null));
        }
        return dto;
    }

    @Override
    public List<ItemResponseDto> getUserItems(Long userId, int from, int size) {
        log.debug("Getting items for user: {}, from: {}, size: {}", userId, from, size);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new EntityNotFoundException("User not found: " + userId);
                });
        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        return itemRepository.findByOwnerIdOrderById(userId, page).stream()
                .map(item -> {
                    List<CommentDto> comments = commentRepository.findByItemId(item.getId()).stream()
                            .map(CommentMapper::toDto)
                            .collect(Collectors.toList());
                    ItemResponseDto dto = ItemMapper.toItemResponseDto(item, comments);
                    dto.setLastBooking(bookingRepository.findLastBooking(item.getId(), now)
                            .map(BookingMapper::toShortDto).orElse(null));
                    dto.setNextBooking(bookingRepository.findNextBooking(item.getId(), now)
                            .map(BookingMapper::toShortDto).orElse(null));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, int from, int size) {
        log.debug("Searching items with text: '{}', from: {}, size: {}", text, from, size);
        if (text == null || text.isBlank()) {
            log.info("Search text is empty, returning empty list");
            return List.of();
        }
        PageRequest page = PageRequest.of(from / size, size);
        return itemRepository.search(text, page).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}