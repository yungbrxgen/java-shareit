package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ItemRequestMapperTest {

    private final ItemRequestMapper requestMapper = Mappers.getMapper(ItemRequestMapper.class);

    @Test
    void toItemRequest_shouldMapFieldsCorrectly() {
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
    void toItemRequestDto_shouldMapWithItemsList() {
        User requestor = new User(2L, "Petr", "petr@mail.ru");
        ItemRequest request = ItemRequest.builder()
                .id(5L)
                .description("Нужен генератор")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemDto itemDto = ItemDto.builder().id(10L).name("Генератор").build();

        ItemRequestDto dto = requestMapper.toItemRequestDto(request, List.of(itemDto));

        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals("Нужен генератор", dto.getDescription());
        assertEquals(1, dto.getItems().size());
        assertEquals(10L, dto.getItems().get(0).getId());
    }
}