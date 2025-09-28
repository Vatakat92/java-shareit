package ru.practicum.shareit.item.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.Create;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemService itemService;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @Validated(Create.class) @RequestBody ItemDto itemDto) {
        log.info("Item API create: userId={} name={}", userId, itemDto.getName());
        ItemDto created = itemService.createItem(userId, itemDto);
        URI location = URI.create("/items/" + created.getId());
        log.info("Item API created: id={} name={}", created.getId(), created.getName());
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId,
                                              @RequestBody ItemDto itemDto) {
        log.info("Item API update: userId={} itemId={}", userId, itemId);

        // Валидация на null
        if (itemDto == null) {
            log.warn("Item data is null for userId={}, itemId={}", userId, itemId);
            return ResponseEntity.badRequest().build();
        }

        // Валидация полей, если они присутствуют в запросе
        if (itemDto.getName() != null && itemDto.getName().isBlank()) {
            log.warn("Item name cannot be empty for userId={}, itemId={}", userId, itemId);
            return ResponseEntity.badRequest().build();
        }

        if (itemDto.getDescription() != null && itemDto.getDescription().isBlank()) {
            log.warn("Item description cannot be empty for userId={}, itemId={}", userId, itemId);
            return ResponseEntity.badRequest().build();
        }

        // Вызов сервиса
        ItemDto updated = itemService.updateItem(userId, itemId, itemDto);
        if (updated == null) {
            log.warn("Item not found or update failed: userId={}, itemId={}", userId, itemId);
            return ResponseEntity.notFound().build();
        }

        log.info("Item API updated: id={} name={}", updated.getId(), updated.getName());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId) {
        log.info("Get item with id={}, userId={}", itemId, userId);

        ItemResponseDto item = itemService.getItem(itemId, userId);

        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getUserItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                              @RequestParam(defaultValue = "0") @Min(0) int from,
                                                              @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.debug("Item API list user items: userId={} from={} size={}", userId, from, size);
        List<ItemResponseDto> items = itemService.getUserItems(userId, from, size);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam String text,
                                                     @RequestParam(defaultValue = "0") @Min(0) int from,
                                                     @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.debug("Item API search: text={} from={} size={}", text, from, size);
        List<ItemDto> items = itemService.searchItems(text, from, size);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @PathVariable Long itemId,
                                                    @Validated(Create.class) @RequestBody CommentDto commentDto) {
        log.info("Comment API create: userId={} itemId={}", userId, itemId);
        CommentDto created = commentService.createComment(userId, itemId, commentDto);
        log.info("Comment API created: id={} itemId={}", created.getId(), itemId);
        URI location = URI.create("/items/" + itemId + "/comments/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }
}