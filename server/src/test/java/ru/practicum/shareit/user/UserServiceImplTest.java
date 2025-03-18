package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "John Doe", "john@example.com");
        userDto = new UserDto(1L, "John Doe", "john@example.com");
    }

    @Test
    void create_WithValidData_ShouldReturnUserDto() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_WithDuplicateEmail_ShouldThrowConflictException() {
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(ConflictException.class, () -> userService.create(userDto));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_WithInvalidEmail_ShouldThrowValidationException() {
        userDto.setEmail("");

        assertThrows(ValidationException.class, () -> userService.create(userDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_WithMalformedEmail_ShouldThrowValidationException() {
        userDto.setEmail("invalid-email");

        assertThrows(ValidationException.class, () -> userService.create(userDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_WithExistingUser_ShouldReturnUpdatedUserDto() {
        User updatedUser = new User(1L, "Updated Name", "updated@example.com");
        UserDto updateDto = new UserDto(1L, "Updated Name", "updated@example.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(1L, updateDto);

        assertNotNull(result);
        assertEquals(updateDto.getName(), result.getName());
        assertEquals(updateDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(1L, userDto));
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_WithDuplicateEmail_ShouldThrowConflictException() {
        UserDto updateDto = new UserDto(1L, "John Doe", "different@example.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        assertThrows(ConflictException.class, () -> userService.update(1L, updateDto));
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        User originalUser = new User(1L, "John Doe", "john@example.com");
        User updatedUser = new User(1L, "Updated Name", "john@example.com");
        UserDto updateDto = new UserDto(null, "Updated Name", null);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(originalUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getById_WithExistingUser_ShouldReturnUserDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getById_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(1L));
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getAll_ShouldReturnListOfUserDto() {
        List<User> users = List.of(
                new User(1L, "John Doe", "john@example.com"),
                new User(2L, "Jane Doe", "jane@example.com")
        );

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Doe", result.get(1).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void delete_WithExistingUser_ShouldDeleteUser() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        userService.delete(1L);

        verify(userRepository, times(1)).existsById(anyLong());
        verify(userRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void delete_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.delete(1L));
        verify(userRepository, times(1)).existsById(anyLong());
        verify(userRepository, never()).deleteById(anyLong());
    }
}