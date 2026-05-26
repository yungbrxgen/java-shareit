package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ServicesIntegrationTest {

    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;
    private final ItemRequestService requestService;

    @Test
    void testUserService_Integration() {
        UserDto userDto = UserDto.builder().name("IntegrationUser").email("integration@mail.ru").build();
        UserDto savedUser = userService.create(userDto);

        assertNotNull(savedUser.getId());

        UserDto retrievedUser = userService.getById(savedUser.getId());
        assertEquals("IntegrationUser", retrievedUser.getName());
        assertEquals("integration@mail.ru", retrievedUser.getEmail());
    }

    @Test
    void testItemService_Integration() {
        UserDto owner = userService.create(UserDto.builder().name("Owner").email("owner@mail.ru").build());

        ItemDto itemDto = ItemDto.builder().name("Дрель").description("Интеграционная").available(true).build();
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        assertNotNull(savedItem.getId());

        List<ItemDto> items = itemService.getAllByOwner(owner.getId());
        assertEquals(1, items.size());
        assertEquals("Дрель", items.get(0).getName());
    }

    @Test
    void testBookingService_Integration() {
        UserDto owner = userService.create(UserDto.builder().name("Owner").email("owner2@mail.ru").build());
        UserDto booker = userService.create(UserDto.builder().name("Booker").email("booker2@mail.ru").build());
        ItemDto item = itemService.create(owner.getId(), ItemDto.builder().name("Отвертка").description("Длинная").available(true).build());

        BookingDto bookingDto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingOutDto savedBooking = bookingService.create(booker.getId(), bookingDto);

        assertNotNull(savedBooking.getId());
        assertEquals(item.getId(), savedBooking.getItem().getId());
        assertEquals(booker.getId(), savedBooking.getBooker().getId());
    }

    @Test
    void testRequestService_Integration() {
        UserDto user = userService.create(UserDto.builder().name("Requester").email("req@mail.ru").build());

        ItemRequestDto savedRequest = requestService.createRequest(user.getId(), "Нужна лестница");

        assertNotNull(savedRequest.getId());
        assertEquals("Нужна лестница", savedRequest.getDescription());
        assertTrue(savedRequest.getItems().isEmpty());
    }
}