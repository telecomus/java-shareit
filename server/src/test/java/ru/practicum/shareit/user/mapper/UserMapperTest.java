package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserMapperTest {

    @Test
    void toUserDto_WithValidUser_ShouldReturnUserDto() {
        // Arrange
        User user = new User(1L, "John Doe", "john@example.com");

        // Act
        UserDto userDto = UserMapper.toUserDto(user);

        // Assert
        assertEquals(1L, userDto.getId());
        assertEquals("John Doe", userDto.getName());
        assertEquals("john@example.com", userDto.getEmail());
    }

    @Test
    void toUserDto_WithNullUser_ShouldHandleNullSafely() {
        // Act
        UserDto userDto = UserMapper.toUserDto(null);

        // Assert
        assertNull(userDto);
    }

    @Test
    void toUser_WithValidUserDto_ShouldReturnUser() {
        // Arrange
        UserDto userDto = new UserDto(1L, "John Doe", "john@example.com");

        // Act
        User user = UserMapper.toUser(userDto);

        // Assert
        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    void toUser_WithNullUserDto_ShouldHandleNullSafely() {
        // Act
        User user = UserMapper.toUser(null);

        // Assert
        assertNull(user);
    }
}