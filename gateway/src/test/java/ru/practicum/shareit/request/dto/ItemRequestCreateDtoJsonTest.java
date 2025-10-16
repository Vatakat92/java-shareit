package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestCreateDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestCreateDto> json;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldSerializeItemRequestCreateDto() throws Exception {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("Need a drill for home repairs");

        JsonContent<ItemRequestCreateDto> result = json.write(requestDto);

        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a drill for home repairs");
    }

    @Test
    void shouldDeserializeItemRequestCreateDto() throws Exception {
        String content = "{\"description\":\"Need a drill for home repairs\"}";

        ItemRequestCreateDto result = json.parseObject(content);

        assertThat(result.getDescription()).isEqualTo("Need a drill for home repairs");
    }

    @Test
    void shouldFailValidationWhenDescriptionIsNull() {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription(null);

        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(requestDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Description cannot be empty");
    }

    @Test
    void shouldFailValidationWhenDescriptionIsBlank() {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("");
        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(requestDto);

        assertThat(violations).hasSize(2); // Ожидаем две ошибки
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Description cannot be empty",
                        "Description must be between 1 and 2000 characters"
                );
    }

    @Test
    void shouldFailValidationWhenDescriptionIsTooLong() {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        String longDescription = "a".repeat(2001);
        requestDto.setDescription(longDescription);

        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(requestDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Description must be between 1 and 2000 characters");
    }

    @Test
    void shouldPassValidationWhenDescriptionIsValid() {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("Valid description");

        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(requestDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassValidationWhenDescriptionIsExactlyMaxLength() {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        String maxLengthDescription = "a".repeat(2000);
        requestDto.setDescription(maxLengthDescription);

        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(requestDto);

        assertThat(violations).isEmpty();
    }
}