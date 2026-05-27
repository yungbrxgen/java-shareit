package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testItemRequestDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 5, 26, 12, 0, 0);

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна пила")
                .created(created)
                .items(Collections.emptyList())
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).hasJsonPathArrayValue("$.items");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна пила");
    }
}