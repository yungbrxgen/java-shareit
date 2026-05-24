
package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;
    private Comment comment;
    private CommentDto commentDto;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@mail.ru");
        booker = new User(2L, "Booker", "booker@mail.ru");
        item = Item.builder().id(1L).name("Дрель").description("Мощная").available(true).owner(owner).build();
        itemDto = ItemDto.builder().id(1L).name("Дрель").description("Мощная").available(true).build();

        comment = Comment.builder().id(1L).text("Супер").author(booker).item(item).created(LocalDateTime.now()).build();
        commentDto = CommentDto.builder().id(1L).text("Супер").authorName("Booker").build();

        lastBooking = Booking.builder().id(1L).item(item).booker(booker).status(BookingStatus.APPROVED).build();
        nextBooking = Booking.builder().id(2L).item(item).booker(booker).status(BookingStatus.APPROVED).build();
    }

    @Test
    void create_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(any(), any(), any())).thenReturn(item);
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toItemDto(any())).thenReturn(itemDto);

        ItemDto result = itemService.create(1L, itemDto);

        assertNotNull(result);
        verify(itemRepository).save(any());
    }

    @Test
    void update_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toItemDto(any())).thenReturn(itemDto);

        ItemDto patch = ItemDto.builder().name("New Name").build();
        ItemDto result = itemService.update(1L, 1L, patch);

        assertNotNull(result);
        assertEquals("Дрель", result.getName());
    }

    @Test
    void getById_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDto(any())).thenReturn(itemDto);
        when(commentRepository.findAllByItemId(1L)).thenReturn(List.of(comment));

        when(bookingRepository.findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(anyLong(), any(), any()))
                .thenReturn(Optional.of(lastBooking));

        ItemDto result = itemService.getById(1L, 1L);

        assertNotNull(result);
        verify(commentRepository).findAllByItemId(1L);
    }

    @Test
    void getAllByOwner_success() {
        when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of(item));
        when(itemMapper.toItemDto(any())).thenReturn(itemDto);
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.getAllByOwner(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void search_success() {
        when(itemRepository.search("дрель")).thenReturn(List.of(item));
        when(itemMapper.toItemDto(any())).thenReturn(itemDto);

        List<ItemDto> result = itemService.search("дрель");

        assertEquals(1, result.size());
    }

    @Test
    void addComment_success() {
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.save(any())).thenReturn(comment);
        when(commentMapper.toCommentDto(any())).thenReturn(commentDto);

        CommentDto result = itemService.addComment(2L, 1L, commentDto);

        assertNotNull(result);
        assertEquals("Супер", result.getText());
    }

    @Test
    void addComment_fail_whenNoBooking() {
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.addComment(2L, 1L, commentDto));
    }

    @Test
    void update_fail_whenUserNotOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDto patch = ItemDto.builder().name("New Name").build();

        assertThrows(NotFoundException.class, () -> itemService.update(99L, 1L, patch));

        verify(itemRepository, never()).save(any());
    }
}
