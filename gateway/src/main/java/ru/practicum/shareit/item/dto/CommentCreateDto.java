package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentCreateDto {
    @NotBlank(message = "Текст комментария должен быть указан")
    String text;
    LocalDateTime created = LocalDateTime.now();
}

