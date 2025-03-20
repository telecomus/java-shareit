package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemRequestMapperTest {

    @Test
    void toItemRequestDto_WithValidItemRequest_ShouldReturnItemRequestDto() {
        // Arrange
        User requestor = new User(1L, "Requestor", "requestor@example.com");
        LocalDateTime created = LocalDateTime.now();

        ItemRequest itemRequest = new ItemRequest(1L, "Need a drill", requestor, created);

        List<ItemDto> items = new ArrayList<>();
        items.add(new ItemDto(1L, "Drill", "Electric drill", true, 1L));

        // Act
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest, items);

        // Assert
        assertEquals(1L, itemRequestDto.getId());
        assertEquals("Need a drill", itemRequestDto.getDescription());
        assertEquals(1L, itemRequestDto.getRequestorId());
        assertEquals(created, itemRequestDto.getCreated());
        assertEquals(1, itemRequestDto.getItems().size());
        assertEquals("Drill", itemRequestDto.getItems().get(0).getName());
    }

    @Test
    void toItemRequestDto_WithEmptyItemsList_ShouldReturnItemRequestDtoWithEmptyList() {
        // Arrange
        User requestor = new User(1L, "Requestor", "requestor@example.com");
        LocalDateTime created = LocalDateTime.now();

        ItemRequest itemRequest = new ItemRequest(1L, "Need a drill", requestor, created);

        List<ItemDto> items = new ArrayList<>();

        // Act
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest, items);

        // Assert
        assertEquals(1L, itemRequestDto.getId());
        assertEquals("Need a drill", itemRequestDto.getDescription());
        assertEquals(1L, itemRequestDto.getRequestorId());
        assertEquals(created, itemRequestDto.getCreated());
        assertEquals(0, itemRequestDto.getItems().size());
    }

    @Test
    void toItemRequestDto_WithNullItemRequest_ShouldHandleNullSafely() {
        // Arrange
        List<ItemDto> items = new ArrayList<>();
        items.add(new ItemDto(1L, "Drill", "Electric drill", true, 1L));

        // Act
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(null, items);

        // Assert
        assertNull(itemRequestDto);
    }
}