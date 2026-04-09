package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
        }

        items.put(item.getId(), item);
        log.debug("Сохранен предмет: {}", item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findByOwnerId(Long id) {
        return items.values().stream()
                .filter(item -> item.getOwnerId() != null && item.getOwnerId().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findByRequestId(Long id) {
        return items.values().stream()
                .filter(item -> item.getRequestId() != null && item.getRequestId().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        String query = text.toLowerCase();

        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(query) || item.getDescription().toLowerCase().contains(query))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (items.remove(id) != null) {
            log.debug("Предмет с ID {} удален", id);
        } else {
            log.warn("Попытка удалить несуществующий предмет с ID {}", id);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return items.containsKey(id);
    }
}
