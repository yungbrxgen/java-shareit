package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public List<BookingOutDto> getAllByOwner(Long userId, String stateStr, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        BookingState state = BookingState.from(stateStr)
                .orElseThrow(() -> new ValidationException("Неизвестное состояние: " + stateStr));

        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        Page<Booking> bookings;

        switch (state) {
            case ALL -> bookings = bookingRepository.findAllByItemOwnerId(userId, pageable);
            case CURRENT ->
                    bookings = bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, now, pageable);
            case PAST -> bookings = bookingRepository.findAllByItemOwnerIdAndEndBefore(userId, now, pageable);
            case FUTURE -> bookings = bookingRepository.findAllByItemOwnerIdAndStartAfter(userId, now, pageable);
            case WAITING ->
                    bookings = bookingRepository.findAllByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, pageable);
            case REJECTED ->
                    bookings = bookingRepository.findAllByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
            default -> bookings = Page.empty();
        }

        return bookings.getContent().stream()
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
    public List<BookingOutDto> getAllByUser(Long userId, String stateStr, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        BookingState state = BookingState.from(stateStr)
                .orElseThrow(() -> new ValidationException("Неизвестное состояние :" + stateStr));

        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
        Page<Booking> bookings;

        switch (state) {
            case ALL -> bookings = bookingRepository
                    .findAllByBookerId(userId, pageable);
            case CURRENT -> bookings = bookingRepository
                    .findAllByBookerIdAndStartBeforeAndEndAfter(userId, now, now, pageable);
            case PAST -> bookings = bookingRepository
                    .findAllByBookerIdAndEndBefore(userId, now, pageable);
            case FUTURE -> bookings = bookingRepository
                    .findAllByBookerIdAndStartAfter(userId, now, pageable);
            case WAITING -> bookings = bookingRepository
                    .findAllByBookerIdAndStatus(userId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookings = bookingRepository
                    .findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
            default -> bookings = Page.empty();
        }
        return bookings.getContent().stream()
                .map(bookingMapper::toBookingOutDto)
                .collect(Collectors.toList());
    }
}
