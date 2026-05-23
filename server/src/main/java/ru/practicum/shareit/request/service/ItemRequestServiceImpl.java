package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ItemRequestMapper itemRequestMapper;

    @Transactional
    @Override
    public ItemRequestDto createRequest(Long userId, String description) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest itemRequest = ItemRequest.builder()
                .description(description)
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Created item request with ID {}", savedRequest.getId());

        return itemRequestMapper.toItemRequestDto(savedRequest, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getMyRequests(Long userId, Sort sort) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(userId, sort);
        return mapRequestsToDto(requests);
    }

    @Override
    public List<ItemRequestDto> getAllOtherRequests(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("created").descending());
        Page<ItemRequest> requestsPage = itemRequestRepository.findByRequestorIdNot(userId, pageRequest);

        return mapRequestsToDto(requestsPage.getContent());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        return mapRequestToDto(itemRequest);
    }

    private ItemRequestDto mapRequestToDto(ItemRequest itemRequest) {
        List<Item> items = itemRepository.findByRequestId(itemRequest.getId());
        List<ItemDto> itemsDto = items.stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());

        return itemRequestMapper.toItemRequestDto(itemRequest, itemsDto);
    }

    private List<ItemRequestDto> mapRequestsToDto(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestsIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> itemsForRequest = itemRepository.findByRequestIdIn(requestsIds);

        Map<Long, List<ItemDto>> itemsByRequestId = itemsForRequest.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(itemMapper::toItemDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> itemRequestMapper.toItemRequestDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

}
