package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient client;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(USER_ID_HEADER) Long userId,
                                                @RequestBody @Valid ItemRequestDto requestDto) {
        log.info("Creating request from user with ID {}", userId);
        return client.createRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getMyRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Get requests user with ID {}", userId);
        return client.getMyRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOtherRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                                      @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                      @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Get other request from user with ID {}, parameters: from = {}, size = {}", userId, from, size);
        return client.getAllOtherRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Get request with ID {}, from user with ID {}", requestId, userId);
        return client.getRequestById(userId, requestId);
    }
}
