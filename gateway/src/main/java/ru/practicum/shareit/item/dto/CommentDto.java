package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.interfaces.Create;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Отзыв не может быть пустым")
    @Size(groups = Create.class, max = 2000, message = "Текст комментария слишком длинный")
    private String text;

    private String authorName;
    private LocalDateTime created;
}
