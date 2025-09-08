package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> storage = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> userItems = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public List<Item> findByUserId(long userId) {
        Set<Long> itemIds = userItems.getOrDefault(userId, Collections.emptySet());
        return itemIds.stream()
                .map(storage::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Item save(Item item) {
        Long itemId = item.getId();
        boolean isNew = (itemId == null);

        if (isNew) {
            itemId = idSequence.incrementAndGet();
            item.setId(itemId);
        } else {
            Item existing = storage.get(itemId);
            if (existing != null && existing.getOwner() != null) {
                Long oldOwnerId = existing.getOwner().getId();
                if (oldOwnerId != null) {
                    Long finalItemId = itemId;
                    userItems.computeIfPresent(oldOwnerId, (k, v) -> {
                        v.remove(finalItemId);
                        return v.isEmpty() ? null : v;
                    });
                }
            }
        }

        storage.put(itemId, item);

        if (item.getOwner() != null && item.getOwner().getId() != null) {
            Long ownerId = item.getOwner().getId();
            userItems.computeIfAbsent(ownerId, k -> ConcurrentHashMap.newKeySet())
                    .add(itemId);
        }

        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(storage.values());
    }
}
