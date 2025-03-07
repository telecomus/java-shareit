package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);
    UserDto update(long userId, UserDto userDto);
    UserDto getById(long userId);
    List<UserDto> getAll();
    void delete(long userId);
}