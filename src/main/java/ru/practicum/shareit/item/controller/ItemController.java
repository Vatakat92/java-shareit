package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import org.springframework.validation.annotation.Validated;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
@Validated
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @Validated(Create.class) @RequestBody ItemDto dto) {
        log.info("Create item request: ownerId={} name={}", userId, dto.getName());
        ItemDto created = itemService.createItem(dto, userId);
        URI location = URI.create("/items/" + created.getId());
        log.info("Item created: id={} ownerId={}", created.getId(), userId);
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long itemId,
                              @Validated(Update.class) @RequestBody ItemDto dto) {
        log.info("Update item request: id={} ownerId={} name={} available={}",
                itemId, userId, dto != null ? dto.getName() : null, dto != null ? dto.getAvailable() : null);
        return itemService.updateItem(itemId, dto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        log.debug("Get item by id: {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Get items by owner: {}", userId);
        return itemService.getAllItemsByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.debug("Search items: text='{}'", text);
        return itemService.searchItems(text);
    }
}