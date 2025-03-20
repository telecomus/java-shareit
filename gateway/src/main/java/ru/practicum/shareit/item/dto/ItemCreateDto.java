package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemCreateDto {
    @NotBlank(message = "Имя должно быть указано")
    String name;

    @NotBlank(message = "Описание должно быть указано")
    String description;

    @NotNull(message = "Доступность должна быть указана")
    Boolean available;

    Long requestId;
}

