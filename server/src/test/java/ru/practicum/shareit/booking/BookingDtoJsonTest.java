package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testBookingDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 12, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 2, 12, 0, 0);

        BookingDto dto = BookingDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.itemId");
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
    }
}