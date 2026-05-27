package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ItemMapperTest {

    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    @Test
    void toItemDto_shouldMapFieldsCorrectly() {
        User owner = new User(1L, "Ivan", "ivan@mail.ru");
        Item item = Item.builder()
                .id(10L)
                .name("Лопата")
                .description("Садовая")
                .available(true)
                .owner(owner)
                .build();

        ItemDto dto = itemMapper.toItemDto(item);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Лопата", dto.getName());
        assertEquals("Садовая", dto.getDescription());
        assertEquals(true, dto.getAvailable());
    }
}