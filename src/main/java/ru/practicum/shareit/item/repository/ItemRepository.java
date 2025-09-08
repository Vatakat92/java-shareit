package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {

    List<Item> findByUserId(long userId);

    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findAll();
}