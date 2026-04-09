package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.interfaces.Create;
import ru.practicum.shareit.interfaces.Update;

@Data
@Builder
public class UserDto {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым", groups = Create.class)
    private String name;
    @NotBlank(message = "Email не может быть пустым", groups = Create.class)
    @Email(message = "Некорректный формат email", groups = {Create.class, Update.class})
    private String email;
}
