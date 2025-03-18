package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateDto {
    @NotBlank(message = "Имя должно быть указано")
    String name;

    @NotBlank(message = "Email должен быть указан")
    @Email(message = "Email должен соответствовать паттерну email")
    String email;
}

