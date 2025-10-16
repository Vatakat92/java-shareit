package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> commentJson;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldSerializeCommentDto() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");
        commentDto.setItemId(1L);

        JsonContent<CommentDto> result = commentJson.write(commentDto);

        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Great item!");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
    }

    @Test
    void shouldDeserializeCommentDto() throws Exception {
        String content = "{\"text\":\"Great item!\",\"itemId\":1}";

        CommentDto result = commentJson.parseObject(content);

        assertThat(result.getText()).isEqualTo("Great item!");
        assertThat(result.getItemId()).isEqualTo(1L);
    }

    @Test
    void shouldSerializeCommentDtoWithAllFields() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item!");
        commentDto.setItemId(10L);
        commentDto.setAuthorName("John Doe");
        commentDto.setCreated(LocalDateTime.of(2023, 1, 1, 12, 0));

        JsonContent<CommentDto> result = commentJson.write(commentDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Great item!");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("John Doe");
        assertThat(result).hasJsonPathValue("$.created");
    }

    @Test
    void shouldDeserializeCommentDtoWithAllFields() throws Exception {
        String content = "{\"id\":1,\"text\":\"Great item!\",\"itemId\":10," +
                "\"authorName\":\"John Doe\",\"created\":\"2023-01-01T12:00:00\"}";

        CommentDto result = commentJson.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Great item!");
        assertThat(result.getItemId()).isEqualTo(10L);
        assertThat(result.getAuthorName()).isEqualTo("John Doe");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
    }

    @Test
    void shouldFailValidationWhenTextIsBlank() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText(""); // Пустой текст
        commentDto.setItemId(1L);

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertThat(violations).hasSize(2); // Ожидаем две ошибки
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Comment text cannot be blank",
                        "Comment text must be between 1 and 1000 characters"
                );
    }

    @Test
    void shouldFailValidationWhenItemIdIsNull() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Item ID is required");
    }

    @Test
    void shouldFailValidationWhenItemIdIsNotPositive() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");
        commentDto.setItemId(-1L); // Negative ID

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(commentDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Item ID must be positive");
    }
}