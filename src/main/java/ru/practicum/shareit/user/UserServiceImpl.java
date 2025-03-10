package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public UserDto create(UserDto userDto) {
        validateNewUser(userDto);
        User user = UserMapper.toUser(userDto);
        user.setId(nextId++);
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (userDto.getEmail() != null) {
            validateEmailUnique(userDto.getEmail(), userId);
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getById(long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        users.remove(userId);
    }

    private void validateNewUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!userDto.getEmail().contains("@")) {
            throw new ValidationException("Некорректный формат email");
        }
        validateEmailUnique(userDto.getEmail(), null);
    }

    private void validateEmailUnique(String email, Long userId) {
        boolean emailExists = users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email) &&
                        (userId == null || !user.getId().equals(userId)));
        if (emailExists) {
            throw new ConflictException("Пользователь с email " + email + " уже существует");
        }
    }
}