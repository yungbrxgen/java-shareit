package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceIntegrationTest {

    private final ItemService itemService;
    private final EntityManager em;

    @Test
    void getAllByOwner_shouldReturnItemsByOwnerId() {
        User owner = new User(null, "Owner", "owner@mail.ru");
        em.persist(owner);

        Item item = Item.builder()
                .name("Дрель")
                .description("Интеграционная")
                .available(true)
                .owner(owner)
                .build();
        em.persist(item);
        em.flush();

        List<ItemDto> items = itemService.getAllByOwner(owner.getId());

        assertFalse(items.isEmpty());
        assertEquals(1, items.size());
        assertEquals("Дрель", items.get(0).getName());
    }
}