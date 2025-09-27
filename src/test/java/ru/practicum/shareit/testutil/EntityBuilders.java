package ru.practicum.shareit.testutil;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * Builders for creating test entities
 */
public class EntityBuilders {

    public static class UserBuilder {
        private final User user = new User();

        public UserBuilder id(Long id) {
            user.setId(id);
            return this;
        }

        public UserBuilder name(String name) {
            user.setName(name);
            return this;
        }

        public UserBuilder email(String email) {
            user.setEmail(email);
            return this;
        }

        public User build() {
            return user;
        }
    }

    public static class ItemBuilder {
        private final Item item = new Item();

        public ItemBuilder id(Long id) {
            item.setId(id);
            return this;
        }

        public ItemBuilder name(String name) {
            item.setName(name);
            return this;
        }

        public ItemBuilder description(String description) {
            item.setDescription(description);
            return this;
        }

        public ItemBuilder available(Boolean available) {
            item.setAvailable(available);
            return this;
        }

        public ItemBuilder owner(User owner) {
            item.setOwner(owner);
            return this;
        }

        public Item build() {
            return item;
        }
    }

    public static class BookingBuilder {
        private final Booking booking = new Booking();

        public BookingBuilder id(Long id) {
            booking.setId(id);
            return this;
        }

        public BookingBuilder start(LocalDateTime start) {
            booking.setStart(start);
            return this;
        }

        public BookingBuilder end(LocalDateTime end) {
            booking.setEnd(end);
            return this;
        }

        public BookingBuilder status(BookingStatus status) {
            booking.setStatus(status);
            return this;
        }

        public BookingBuilder booker(User booker) {
            booking.setBooker(booker);
            return this;
        }

        public BookingBuilder item(Item item) {
            booking.setItem(item);
            return this;
        }

        public Booking build() {
            return booking;
        }
    }

    public static class CommentBuilder {
        private final Comment comment = new Comment();

        public CommentBuilder id(Long id) {
            comment.setId(id);
            return this;
        }

        public CommentBuilder text(String text) {
            comment.setText(text);
            return this;
        }

        public CommentBuilder created(LocalDateTime created) {
            comment.setCreated(created);
            return this;
        }

        public CommentBuilder author(User author) {
            comment.setAuthor(author);
            return this;
        }

        public CommentBuilder item(Item item) {
            comment.setItem(item);
            return this;
        }

        public Comment build() {
            return comment;
        }
    }

    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static ItemBuilder item() {
        return new ItemBuilder();
    }

    public static BookingBuilder booking() {
        return new BookingBuilder();
    }

    public static CommentBuilder comment() {
        return new CommentBuilder();
    }
}
