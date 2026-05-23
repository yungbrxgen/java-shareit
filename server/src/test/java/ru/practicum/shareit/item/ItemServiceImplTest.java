package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User otherUser;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@mail.ru");
        otherUser = new User(2L, "User", "user@mail.ru");

        item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Хорошая дрель")
                .available(true)
                .owner(owner)
                .build();

        itemDto = ItemDto.builder()
                .name("Дрель обновленная")
                .description("Обновленное описание")
                .available(true)
                .build();
    }

    @Test
    void updateItem_whenUserIsOwner_thenUpdateSuccess() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);
        when(itemMapper.toItemDto(any())).thenReturn(itemDto);

        ItemDto result = itemService.update(1L, 1L, itemDto);

        assertNotNull(result);
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void search_whenTextIsEmpty_thenReturnEmptyList() {
        List<ItemDto> result = itemService.search("");

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).search(anyString());
    }

    @Test
    void search_success() {
        when(itemRepository.search("Отвертка")).thenReturn(List.of(item));
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        List<ItemDto> result = itemService.search("Отвертка");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(itemRepository).search("Отвертка");
    }

    @Test
    void addComment_success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(otherUser));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(true);

        Comment comment = new Comment();
        when(commentRepository.save(any())).thenReturn(comment);

        CommentDto respDto = CommentDto.builder().text("Все было отлично").build();
        when(commentMapper.toCommentDto(any())).thenReturn(respDto);

        CommentDto commentDto = CommentDto.builder().text("Все было отлично").build();

        CommentDto result = itemService.addComment(otherUser.getId(), item.getId(), commentDto);

        assertNotNull(result);
        assertEquals("Все было отлично", result.getText());
    }

    @Test
    void addComment_whenUserHadNotBookedItem_thenThrowValidationException() {
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        CommentDto commentDto = CommentDto.builder().text("Хорошая дрель").build();

        assertThrows(ValidationException.class, () -> itemService.addComment(otherUser.getId(),
                item.getId(), commentDto));

        verify(commentRepository, never()).save(any());
    }
}
