package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingInputDto;
    private BookingOutDto bookingOutDto;

    @BeforeEach
    void setUp() {
        booker = new User(1L, "Booker", "booker@mail.ru");
        owner = new User(2L, "Owner", "owner@mail.ru");
        item = Item.builder()
                .id(1L)
                .name("Item")
                .owner(owner)
                .available(true)
                .build();

        booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        bookingInputDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingOutDto = BookingOutDto.builder()
                .id(1L)
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(ItemDto.builder().id(1L).build())
                .build();
    }

    @Test
    void create_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        when(bookingMapper.toBooking(any(BookingDto.class), any(Item.class), any(User.class)))
                .thenReturn(booking);

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        when(bookingMapper.toBookingOutDto(any(Booking.class))).thenReturn(bookingOutDto);

        BookingOutDto result = bookingService.create(booker.getId(), bookingInputDto);

        assertNotNull(result);
        assertEquals(bookingOutDto.getId(), result.getId());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void create_whenBookerIsOwner_thenThrowNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(2L, bookingInputDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_whenItemNotAvailable_thenThrowValidationException() {
        item.setAvailable(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(1L, bookingInputDto));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approve_whenStatusIsWaiting_thenApprove() {
        booking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        when(bookingMapper.toBookingOutDto(any(Booking.class))).thenReturn(bookingOutDto);

        BookingOutDto result = bookingService.approve(owner.getId(), 1L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void approve_whenUserIsNotOwner_thenThrowValidationException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.approve(booking.getId(), 1L, true));
    }

    @Test
    void approve_whenStatusAlreadyApproved_thenThrowableValidationException() {
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.approve(owner.getId(), 1L, true));
    }

    @Test
    void getById_whenUserIsBooker_thenSuccess() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        when(bookingMapper.toBookingOutDto(any())).thenReturn(bookingOutDto);

        BookingOutDto result = bookingService.getById(booker.getId(), 1L);

        assertNotNull(result);
    }

    @Test
    void getById_whenUserIsStranger_thenThrowNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class,
                () -> bookingService.getById(99L, 1L));
    }

    @Test
    void getAllByBooker_whenStateAll_thenSuccess() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));

        Page<Booking> page = new PageImpl<>(List.of(booking));

        when(bookingRepository.findAllByBookerId(anyLong(), any(Pageable.class)))
                .thenReturn(page);
        when(bookingMapper.toBookingOutDto(any())).thenReturn(bookingOutDto);

        List<BookingOutDto> result = bookingService.getAllByUser(booker.getId(), "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByBookerId(eq(booker.getId()), any(Pageable.class));
    }
}
