package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, String description);

    List<ItemRequestDto> getMyRequests(Long userId, Sort sort);

    List<ItemRequestDto> getAllOtherRequests(Long userId, Integer from, Integer size);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}
