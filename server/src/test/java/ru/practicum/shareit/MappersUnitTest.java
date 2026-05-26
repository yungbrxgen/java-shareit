package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MappersUnitTest {

    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);
    private final ItemRequestMapper requestMapper = Mappers.getMapper(ItemRequestMapper.class);

    @Test
    void testItemMapper_toItemDto() {
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

    @Test
    void testItemRequestMapper_toItemRequest() {
        User requestor = new User(2L, "Petr", "petr@mail.ru");
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Хочу дрель")
                .build();

        LocalDateTime now = LocalDateTime.now();
        ItemRequest entity = requestMapper.toItemRequest(dto, requestor, now);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("Хочу дрель", entity.getDescription());
        assertEquals(requestor, entity.getRequestor());
        assertEquals(now, entity.getCreated());
    }

    @Test
    void testItemRequestMapper_toItemRequestDto() {
        User requestor = new User(2L, "Petr", "petr@mail.ru");
        ItemRequest request = ItemRequest.builder()
                .id(5L)
                .description("Нужен генератор")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequestDto dto = requestMapper.toItemRequestDto(request, Collections.emptyList());

        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals("Нужен генератор", dto.getDescription());
        assertNotNull(dto.getItems());
        assertEquals(0, dto.getItems().size());
    }
}