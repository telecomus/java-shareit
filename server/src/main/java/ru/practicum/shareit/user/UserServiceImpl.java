package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        validateNewUser(userDto);
        try {
            User user = UserMapper.toUser(userDto);
            user = userRepository.save(user);
            log.info("Создан пользователь: {}", user);
            return UserMapper.toUserDto(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }
    }

    @Override
    @Transactional
    public UserDto update(long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            try {
                user.setEmail(userDto.getEmail());
            } catch (DataIntegrityViolationException e) {
                throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
            }
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        try {
            user = userRepository.save(user);
            log.info("Обновлен пользователь: {}", user);
            return UserMapper.toUserDto(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }
    }

    @Override
    public UserDto getById(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        userRepository.deleteById(userId);
        log.info("Удален пользователь с id: {}", userId);
    }

    private void validateNewUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!userDto.getEmail().contains("@")) {
            throw new ValidationException("Некорректный формат email");
        }
    }
}