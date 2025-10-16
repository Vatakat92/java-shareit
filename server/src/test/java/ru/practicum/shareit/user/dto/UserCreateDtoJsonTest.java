package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserCreateDtoJsonTest {

    @Autowired
    private JacksonTester<UserCreateDto> json;

    private UserCreateDto userCreateDto;

    @BeforeEach
    void setUp() {
        userCreateDto = UserCreateDto.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        JsonContent<UserCreateDto> result = json.write(userCreateDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String jsonContent = "{\"name\":\"John Doe\",\"email\":\"john.doe@example.com\"}";

        UserCreateDto result = json.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldHaveCorrectToString() {
        String expected = "UserCreateDto(name=John Doe, email=john.doe@example.com)";
        assertThat(userCreateDto.toString()).isEqualTo(expected);
    }
}