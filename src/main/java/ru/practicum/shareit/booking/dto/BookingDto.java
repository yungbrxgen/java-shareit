package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    private Long id;

    @NotNull(message = "Дата начала должна быть указана")
    @FutureOrPresent(message = "Дата бронирования не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "Дата завершения должна быть указана ")
    @Future(message = "Дата завершения должна быть в будущем")
    private LocalDateTime end;

    @NotNull(message = "ID предмета должно быть указано")
    private Long itemId;
}
