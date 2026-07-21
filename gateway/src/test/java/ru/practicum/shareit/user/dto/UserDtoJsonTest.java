package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> userDtoJacksonTester;

    @Autowired
    private JacksonTester<UserUpdateDto> userUpdateDtoJacksonTester;

    @Autowired
    private ObjectMapper objectMapper;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void userDtoSerializationTest() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");

        JsonContent<UserDto> result = userDtoJacksonTester.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john@example.com");
    }

    @Test
    void userDtoDeserializationTest() throws Exception {
        String json = "{\"id\": 1, "
                + "\"name\": \"John Doe\", "
                + "\"email\": \"john@example.com\"}";

        UserDto userDto = objectMapper.readValue(json, UserDto.class);

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("John Doe");
        assertThat(userDto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void userDtoValidation_whenInvalidEmail_shouldReturnViolation() {
        UserDto userDto = new UserDto();
        userDto.setEmail("invalid-email");
        userDto.setName("John Doe");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email must be valid");
    }

    @Test
    void userDtoValidation_whenBlankName_shouldReturnViolation() {
        UserDto userDto = new UserDto();
        userDto.setName("");
        userDto.setEmail("john@example.com");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).hasSize(2);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Name cannot be blank",
                        "Name must be between 1 and 255 characters"
                );
    }

    @Test
    void userDtoValidation_whenBlankEmail_shouldReturnViolation() {
        UserDto userDto = new UserDto();
        userDto.setName("John Doe");
        userDto.setEmail("");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email cannot be blank");
    }

    @Test
    void userUpdateDtoSerializationTest() throws Exception {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("Updated Name");
        userUpdateDto.setEmail("updated@example.com");

        JsonContent<UserUpdateDto> result = userUpdateDtoJacksonTester.write(userUpdateDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Updated Name");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("updated@example.com");
    }

    @Test
    void userUpdateDtoWithPartialDataTest() throws Exception {
        String json = "{\"name\": \"Only Name Updated\"}";

        UserUpdateDto userUpdateDto = objectMapper.readValue(json, UserUpdateDto.class);

        assertThat(userUpdateDto.getName()).isNotNull();
        assertThat(userUpdateDto.getName()).isEqualTo("Only Name Updated");
        assertThat(userUpdateDto.getEmail()).isNull();
    }

    @Test
    void userUpdateDtoDeserializationWithNullsTest() throws Exception {
        String json = "{}";

        UserUpdateDto userUpdateDto = objectMapper.readValue(json, UserUpdateDto.class);

        assertThat(userUpdateDto.getName()).isNull();
        assertThat(userUpdateDto.getEmail()).isNull();
    }

    @Test
    void userUpdateDtoValidation_whenInvalidEmail_shouldReturnViolation() {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setEmail("invalid-email");

        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(userUpdateDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email must be valid");
    }
}