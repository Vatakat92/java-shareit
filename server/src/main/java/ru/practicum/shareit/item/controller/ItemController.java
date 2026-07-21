package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDto createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemCreateDto itemDto) {
        log.info("Item API create: userId={} name={}", userId, itemDto.getName());
        ItemResponseDto created = itemService.createItem(userId, itemDto);
        log.info("Item API created: id={} name={}", created.getId(), created.getName());
        return created;
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDto itemDto) {
        log.info("Item API update: userId={} itemId={}", userId, itemId);
        ItemResponseDto updated = itemService.updateItem(userId, itemId, itemDto);
        log.info("Item API updated: id={} name={}", updated.getId(), updated.getName());
        return updated;
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId) {
        log.info("Get item with id={}, userId={}", itemId, userId);
        return itemService.getItem(itemId, userId);
    }

    @GetMapping
    public List<ItemResponseDto> getUserItems(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Item API list user items: userId={} from={} size={}", userId, from, size);
        return itemService.getUserItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItems(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("Item API search: text={} from={} size={}", text, from, size);
        return itemService.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentCreateDto commentDto) {
        log.info("Comment API create: userId={} itemId={}", userId, itemId);
        CommentDto created = commentService.createComment(userId, itemId, commentDto);
        log.info("Comment API created: id={} itemId={}", created.getId(), itemId);
        return created;
    }
}