package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testItemDtoSerialization() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная")
                .available(true)
                .build();

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathBooleanValue("$.available");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Мощная");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
    }

    @Test
    void testItemDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"name\":\"Дрель\",\"description\":\"Мощная\",\"available\":true}";

        ItemDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная");
        assertThat(dto.getAvailable()).isEqualTo(true);
    }
}