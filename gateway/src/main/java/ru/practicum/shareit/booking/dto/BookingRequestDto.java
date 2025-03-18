package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequestDto {
    @NotBlank(message = "Время начала должно быть указано")
    LocalDateTime start;

    @NotBlank(message = "Время окончания должно быть указано")
    LocalDateTime end;

    @NotBlank(message = "Время окончания должно быть указано")
    Long itemId;

    Long bookerId;
}

