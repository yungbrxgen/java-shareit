package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingOutDto toBookingOutDto(Booking booking);

    @Mapping(target = "id", source = "bookingDto.id")
    @Mapping(target = "start", source = "bookingDto.start")
    @Mapping(target = "end", source = "bookingDto.end")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "booker", source = "booker")
    @Mapping(target = "status", ignore = true)
    Booking toBooking(BookingDto bookingDto, Item item, User booker);
}
