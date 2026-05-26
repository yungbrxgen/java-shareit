package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface ItemRequestMapper {

    @Mapping(target = "id", source = "itemRequest.id")
    @Mapping(target = "description", source = "itemRequest.description")
    @Mapping(target = "created", source = "itemRequest.created")
    @Mapping(target = "items", source = "items")
    ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemDto> items);

    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "requestor", source = "requestor")
    @Mapping(target = "created", source = "created")
    ItemRequest toItemRequest(ItemRequestDto dto, User requestor, LocalDateTime created);
}