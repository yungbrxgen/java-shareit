package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.interfaces.Create;
import ru.practicum.shareit.interfaces.Update;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @RequestBody @Validated(Create.class) ItemDto itemDto) {
        log.info("Post new item for user with ID {}", userId);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody @Validated(Update.class) ItemDto itemDto) {
        log.info("Update item with ID {} for user with ID {}", itemId, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Get items by owner for user with ID {}", userId);
        return itemClient.getItemsByOwner(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_ID_HEADER) Long userId,
                                              @PathVariable Long itemId) {
        log.info("Get item with ID {} for user with ID {}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        log.info("Search items with text {}", text);
        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody @Validated(Create.class) CommentDto commentDto) {
        log.info("Adding comment by user ID {} for item ID {}", userId, itemId);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}
