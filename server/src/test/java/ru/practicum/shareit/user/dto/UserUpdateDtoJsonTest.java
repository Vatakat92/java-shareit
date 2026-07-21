package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserUpdateDtoJsonTest {

    @Autowired
    private JacksonTester<UserUpdateDto> json;

    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void setUp() {
        userUpdateDto = UserUpdateDto.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        JsonContent<UserUpdateDto> result = json.write(userUpdateDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String jsonContent = "{\"name\":\"John Doe\",\"email\":\"john.doe@example.com\"}";

        UserUpdateDto result = json.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldHaveCorrectToString() {
        String expected = "UserUpdateDto(name=John Doe, email=john.doe@example.com)";
        assertThat(userUpdateDto.toString()).isEqualTo(expected);
    }
}