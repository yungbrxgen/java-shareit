package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final EntityManager em;

    @Test
    void getById_shouldReturnBooking() {
        User owner = new User(null, "Owner", "owner@mail.ru");
        em.persist(owner);

        User booker = new User(null, "Booker", "booker@mail.ru");
        em.persist(booker);

        Item item = Item.builder()
                .name("Отвертка")
                .description("Длинная")
                .available(true)
                .owner(owner)
                .build();
        em.persist(item);

        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        em.persist(booking);
        em.flush();

        BookingOutDto result = bookingService.getById(booker.getId(), booking.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
    }
}