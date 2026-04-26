package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingOutDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено"));

        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();

        if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
            throw new NotFoundException("Доступ к бронированию запрещен. Вы не являетесь участником сделки");
        }

        return bookingMapper.toBookingOutDto(booking);
    }

    @Override
    public List<BookingOutDto> getAllByOwner(Long userId, String stateStr) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        BookingState state = BookingState.from(stateStr)
                .orElseThrow(() -> new ValidationException("Неизвестное состояние: " + stateStr));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();

        switch (state) {
            case ALL -> bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookings = bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> bookings = bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookings = bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookings = new ArrayList<>();
        }

        return bookings.stream()
                .map(bookingMapper::toBookingOutDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingOutDto create(Long userId, BookingDto bookingDto) {
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может забронировать свою вещь");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) || bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new ValidationException("Дата завершения не может быть раньше или равна дате начала");
        }

        Booking booking = bookingMapper.toBooking(bookingDto, item, booker);

        booking.setStatus(BookingStatus.WAITING);

        return bookingMapper.toBookingOutDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingOutDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Только владелец вещи может подтвердить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return bookingMapper.toBookingOutDto(bookingRepository.save(booking));
    }

    @Override
    public List<BookingOutDto> getAllByUser(Long userId, String stateStr) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        BookingState state = BookingState.from(stateStr)
                .orElseThrow(() -> new ValidationException("Неизвестное состояние :" + stateStr));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL -> bookings = bookingRepository.
                    findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookings = bookingRepository
                    .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> bookings = bookingRepository
                    .findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookings = bookingRepository
                    .findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookings = bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookings = bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookings = new ArrayList<>();
        }
        return bookings.stream().map(bookingMapper::toBookingOutDto)
                .collect(Collectors.toList());
    }
}
