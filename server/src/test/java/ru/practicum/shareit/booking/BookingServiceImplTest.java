
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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;
    private BookingOutDto outDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@mail.ru");
        booker = new User(2L, "Booker", "booker@mail.ru");
        item = Item.builder().id(1L).name("Дрель").available(true).owner(owner).build();

        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        outDto = BookingOutDto.builder().id(1L).status(BookingStatus.WAITING).build();
    }

    @Test
    void getById_success_byBooker() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingOutDto(booking)).thenReturn(outDto);

        BookingOutDto result = bookingService.getById(2L, 1L);

        assertNotNull(result);
        verify(bookingRepository).findById(1L);
    }

    @Test
    void getById_success_byOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingOutDto(booking)).thenReturn(outDto);

        BookingOutDto result = bookingService.getById(1L, 1L);

        assertNotNull(result);
        verify(bookingRepository).findById(1L);
    }

    @Test
    void getById_fail_whenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getById(1L, 99L));
    }

    @Test
    void getById_fail_whenUserNotParticipant() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getById(99L, 1L));
    }

    @Test
    void create_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingMapper.toBooking(any(), any(), any())).thenReturn(booking);
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toBookingOutDto(any())).thenReturn(outDto);

        BookingOutDto result = bookingService.create(2L, bookingDto);

        assertNotNull(result);
        verify(bookingRepository).save(any());
    }

    @Test
    void create_fail_whenItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(2L, bookingDto));
    }

    @Test
    void create_fail_whenUserNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(2L, bookingDto));
    }

    @Test
    void create_fail_whenItemNotAvailable() {
        item.setAvailable(false);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThrows(ValidationException.class, () -> bookingService.create(2L, bookingDto));
    }

    @Test
    void create_fail_whenBookerIsOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(NotFoundException.class, () -> bookingService.create(1L, bookingDto));
    }

    @Test
    void create_fail_whenEndBeforeStart() {
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThrows(ValidationException.class, () -> bookingService.create(2L, bookingDto));
    }

    @Test
    void create_fail_whenEndEqualsStart() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        bookingDto.setStart(time);
        bookingDto.setEnd(time);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThrows(ValidationException.class, () -> bookingService.create(2L, bookingDto));
    }

    @Test
    void approve_success_true() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toBookingOutDto(any())).thenReturn(outDto);

        BookingOutDto result = bookingService.approve(1L, 1L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void approve_success_false() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toBookingOutDto(any())).thenReturn(outDto);

        BookingOutDto result = bookingService.approve(1L, 1L, false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
    }

    @Test
    void approve_fail_whenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.approve(1L, 99L, true));
    }

    @Test
    void approve_fail_whenUserNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.approve(99L, 1L, true));
    }

    @Test
    void approve_fail_whenAlreadyApprovedOrRejected() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () -> bookingService.approve(1L, 1L, true));
    }


    @Test
    void getAllByUser_success_allStates() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        Page<Booking> page = new PageImpl<>(List.of(booking));

        lenient().when(bookingRepository.findAllByBookerId(anyLong(), any(Pageable.class))).thenReturn(page);
        lenient().when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(anyLong(), any(), any(), any(Pageable.class))).thenReturn(page);
        lenient().when(bookingRepository.findAllByBookerIdAndEndBefore(anyLong(), any(), any(Pageable.class))).thenReturn(page);
        lenient().when(bookingRepository.findAllByBookerIdAndStartAfter(anyLong(), any(), any(Pageable.class))).thenReturn(page);
        lenient().when(bookingRepository.findAllByBookerIdAndStatus(anyLong(), any(), any(Pageable.class))).thenReturn(page);

        assertNotNull(bookingService.getAllByUser(2L, "ALL", 0, 10));
        assertNotNull(bookingService.getAllByUser(2L, "CURRENT", 0, 10));
        assertNotNull(bookingService.getAllByUser(2L, "PAST", 0, 10));
        assertNotNull(bookingService.getAllByUser(2L, "FUTURE", 0, 10));
        assertNotNull(bookingService.getAllByUser(2L, "WAITING", 0, 10));
        assertNotNull(bookingService.getAllByUser(2L, "REJECTED", 0, 10));
    }

    @Test
    void getAllByUser_fail_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getAllByUser(99L, "ALL", 0, 10));
    }

    @Test
    void getAllByUser_fail_whenUnknownState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThrows(ValidationException.class, () -> bookingService.getAllByUser(2L, "UNSUPPORTED_STATE", 0, 10));
    }

    @Test
    void getAllByOwner_success_allStates() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        Page<Booking> page = new PageImpl<>(List.of(booking));

        lenient().when(bookingRepository.findAllByItemOwnerId(anyLong(), any(Pageable.class))).thenReturn(page);
        lenient().when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(anyLong(), any(), any(), any(Pageable.class))).thenReturn(page);
        lenient().when(bookingRepository.findAllByItemOwnerIdAndEndBefore(anyLong(), any(), any(Pageable.class))).thenReturn(page);
        lenient().when(bookingRepository.findAllByItemOwnerIdAndStartAfter(anyLong(), any(), any(Pageable.class))).thenReturn(page);
        lenient().when(bookingRepository.findAllByItemOwnerIdAndStatus(anyLong(), any(), any(Pageable.class))).thenReturn(page);

        assertNotNull(bookingService.getAllByOwner(1L, "ALL", 0, 10));
        assertNotNull(bookingService.getAllByOwner(1L, "CURRENT", 0, 10));
        assertNotNull(bookingService.getAllByOwner(1L, "PAST", 0, 10));
        assertNotNull(bookingService.getAllByOwner(1L, "FUTURE", 0, 10));
        assertNotNull(bookingService.getAllByOwner(1L, "WAITING", 0, 10));
        assertNotNull(bookingService.getAllByOwner(1L, "REJECTED", 0, 10));
    }

    @Test
    void getAllByOwner_fail_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getAllByOwner(99L, "ALL", 0, 10));
    }

    @Test
    void getAllByOwner_fail_whenUnknownState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class, () -> bookingService.getAllByOwner(1L, "INVALID", 0, 10));
    }
}
