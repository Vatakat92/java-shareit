package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> storage = new ConcurrentHashMap<>();
    private final Map<String, Long> emailIndex = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getId() == null) {
            user.setId(idSequence.incrementAndGet());
        } else if (storage.containsKey(user.getId())) {
            User existing = storage.get(user.getId());
            if (!existing.getEmail().equals(user.getEmail())) {
                emailIndex.remove(existing.getEmail().toLowerCase());
            }
        }

        Long userId = user.getId();
        storage.put(userId, user);
        if (user.getEmail() != null) {
            emailIndex.put(user.getEmail().toLowerCase(), userId);
        }
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void deleteById(Long id) {
        User user = storage.remove(id);
        if (user != null && user.getEmail() != null) {
            emailIndex.remove(user.getEmail().toLowerCase());
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return emailIndex.containsKey(email.toLowerCase());
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.containsKey(id);
    }
}
