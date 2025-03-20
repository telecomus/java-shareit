package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestCreateDto {
    @NotBlank(message = "Описание должно быть указано")
    String description;
    LocalDateTime created = LocalDateTime.now();
}

