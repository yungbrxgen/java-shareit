package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;


@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService service;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @RequestBody ItemRequestDto requestDto) {
        log.info("Creating request for user with ID {}", userId);
        return service.createRequest(userId, requestDto.getDescription());
    }

    @GetMapping
    public List<ItemRequestDto> getMyRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Get requests from user with ID {}", userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        return service.getMyRequests(userId, sort);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllOtherRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                                    @RequestParam(defaultValue = "0") Integer from,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        log.info("Get other requests from user with ID {}", userId);
        return service.getAllOtherRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @PathVariable Long requestId) {
        log.info("Get request ID {} from user with ID {}", requestId, userId);
        return service.getRequestById(userId, requestId);
    }




}
