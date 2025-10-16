package ru.practicum.shareit.item.dto;

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
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemCreateDto> json;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldSerializeItemDto() throws Exception {
        ItemCreateDto itemDto = new ItemCreateDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(5L);

        JsonContent<ItemCreateDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }

    @Test
    void shouldDeserializeItemDto() throws Exception {
        String content = "{\"name\":\"Test Item\"," +
                "\"description\":\"Test Description\",\"available\":true,\"requestId\":5}";

        ItemCreateDto result = json.parseObject(content);

        assertThat(result.getName()).isEqualTo("Test Item");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getAvailable()).isEqualTo(true);
        assertThat(result.getRequestId()).isEqualTo(5L);
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        ItemCreateDto itemDto = new ItemCreateDto();
        itemDto.setName(""); // Blank name
        itemDto.setDescription("Valid description");
        itemDto.setAvailable(true);

        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(itemDto);

        assertThat(violations).hasSize(2);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Name cannot be empty",
                        "Name must be between 1 and 255 characters"
                );
    }

    @Test
    void shouldFailValidationWhenDescriptionIsBlank() {
        ItemCreateDto itemDto = new ItemCreateDto();
        itemDto.setName("Valid name");
        itemDto.setDescription(""); // Blank description
        itemDto.setAvailable(true);

        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(itemDto);

        assertThat(violations).hasSize(2);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Description cannot be empty",
                        "Description must be between 1 and 2000 characters"
                );
    }

    @Test
    void shouldFailValidationWhenAvailableIsNull() {
        ItemCreateDto itemDto = new ItemCreateDto();
        itemDto.setName("Valid name");
        itemDto.setDescription("Valid description");
        itemDto.setAvailable(null); // Null available

        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(itemDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Available status is required");
    }

    @Test
    void shouldFailValidationWhenRequestIdIsNotPositive() {
        ItemCreateDto itemDto = new ItemCreateDto();
        itemDto.setName("Valid name");
        itemDto.setDescription("Valid description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(-1L); // Negative request ID

        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(itemDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Request ID must be positive");
    }
}