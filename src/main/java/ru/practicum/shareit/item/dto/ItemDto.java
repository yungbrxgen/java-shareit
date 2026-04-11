package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.interfaces.Create;

@Data
@Builder
public class ItemDto {
    private Long id;

    @NotBlank(message = "Название не может быть пустым", groups = Create.class)
    private String name;

    @NotBlank(message = "Описание не может быть пустым", groups = Create.class)
    private String description;

    @NotNull(message = "Статус доступности должен быть указан", groups = Create.class)
    private Boolean available;
    private Long requestId;
}
