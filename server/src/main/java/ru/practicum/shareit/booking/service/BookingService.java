package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

public interface BookingService {
    BookingOutDto create(Long userId, BookingDto bookingDto);

    BookingOutDto approve(Long userId, Long bookingId, Boolean approved);

    BookingOutDto getById(Long userId, Long bookingId);

    List<BookingOutDto> getAllByUser(Long userId, String stateStr, Integer from, Integer size);

    List<BookingOutDto> getAllByOwner(Long userId, String state, Integer from, Integer size);
}
