package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BookingMapperTest {

    private final BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);

    @Test
    void toBookingOutDto_shouldMapFieldsCorrectly() {
        User booker = new User(2L, "Booker", "booker@mail.ru");
        Item item = Item.builder().id(3L).name("Дрель").build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();

        BookingOutDto dto = bookingMapper.toBookingOutDto(booking);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(BookingStatus.APPROVED, dto.getStatus());
        assertEquals(2L, dto.getBooker().getId());
        assertEquals(3L, dto.getItem().getId());
    }
}