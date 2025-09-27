package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class CommentMapper {
    public static CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setItemId(comment.getItem() != null ? comment.getItem().getId() : null);
        dto.setAuthorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null);
        if (comment.getAuthor() != null) {
            dto.setAuthorName(comment.getAuthor().getName());
        }
        dto.setCreated(comment.getCreated());
        return dto;
    }

    public static Comment toEntity(CommentDto dto, Item item, User author) {
        if (dto == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setId(dto.getId());
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(dto.getCreated());
        return comment;
    }
}