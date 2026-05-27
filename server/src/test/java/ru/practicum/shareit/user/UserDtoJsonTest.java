package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testUserDtoSerialization() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.email");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("ivan@mail.ru");
    }

    @Test
    void testUserDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"name\":\"Ivan\",\"email\":\"ivan@mail.ru\"}";

        UserDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Ivan");
        assertThat(dto.getEmail()).isEqualTo("ivan@mail.ru");
    }
}