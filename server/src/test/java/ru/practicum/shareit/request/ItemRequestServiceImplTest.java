package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User user;
    private ItemRequest request;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Ivan", "ivan@mail.ru");
        request = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .items(Collections.emptyList())
                .build();
    }

    @Test
    void createRequest_whenUserExists_thenSaved() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto outputDto = ItemRequestDto.builder().id(1L).description("Нужна дрель").build();
        when(itemRequestMapper.toItemRequestDto(any(), any())).thenReturn(outputDto);

        ItemRequestDto result = requestService.createRequest(1L, "Нужна дрель");

        assertNotNull(result);
        assertEquals("Нужна дрель", result.getDescription());
    }

    @Test
    void getById_whenRequestNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getRequestById(1L, 99L));
    }

    @Test
    void getMyRequests_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorId(eq(1L), any(Sort.class))).thenReturn(List.of(request));
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(Collections.emptyList());
        when(itemRequestMapper.toItemRequestDto(any(), any())).thenReturn(requestDto);

        List<ItemRequestDto> result = requestService.getMyRequests(1L, Sort.unsorted());

        assertFalse(result.isEmpty());
        verify(itemRequestRepository).findByRequestorId(eq(1L), any());
    }

    @Test
    void getAllOtherRequests_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Page<ItemRequest> page = new PageImpl<>(List.of(request));

        when(itemRequestRepository.findByRequestorIdNot(eq(1L), any(Pageable.class))).thenReturn(page);
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(Collections.emptyList());
        when(itemRequestMapper.toItemRequestDto(any(), any())).thenReturn(requestDto);

        List<ItemRequestDto> result = requestService.getAllOtherRequests(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemRequestRepository).findByRequestorIdNot(eq(1L), any());
    }

    @Test
    void getRequestById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        when(itemRepository.findByRequestId(1L)).thenReturn(Collections.emptyList());

        when(itemRequestMapper.toItemRequestDto(any(), any())).thenReturn(requestDto);

        ItemRequestDto result = requestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(itemRequestRepository).findById(1L);
    }

    @Test
    void getRequestById_whenNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getRequestById(1L, 99L));
    }

    @Test
    void createRequest_whenUserNotFound_thenThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.createRequest(99L, "desc"));
    }
}
