package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@RestController
@RequestMapping("/users")
public class UserController {
    private final String usersIdPath = "/{id}";
    private final UserClient userClient;

    @Autowired
    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping()
    public ResponseEntity<Object> findAll() {
        return userClient.findAll();
    }

    @GetMapping(usersIdPath)
    public ResponseEntity<Object> findUser(@PathVariable Long id) {
        return userClient.findById(id);
    }

    @PostMapping()
    public ResponseEntity<Object> create(@Valid @RequestBody UserCreateDto userDto) {
        return userClient.create(userDto);
    }

    @PatchMapping(usersIdPath)
    public ResponseEntity<Object> update(@Valid @RequestBody UserUpdateDto userDto, @PathVariable Long id) {
        return userClient.update(userDto, id);
    }

    @DeleteMapping(usersIdPath)
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        return userClient.delete(id);
    }
}