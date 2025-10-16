package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserShortDtoJsonTest {

    @Autowired
    private JacksonTester<UserShortDto> json;

    private UserShortDto userShortDto;

    @BeforeEach
    void setUp() {
        userShortDto = UserShortDto.builder()
                .id(1L)
                .name("John Doe")
                .build();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        JsonContent<UserShortDto> result = json.write(userShortDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String jsonContent = "{\"id\":1,\"name\":\"John Doe\"}";

        UserShortDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
    }

    @Test
    void shouldHaveCorrectToString() {
        String expected = "UserShortDto(id=1, name=John Doe)";
        assertThat(userShortDto.toString()).isEqualTo(expected);
    }
}