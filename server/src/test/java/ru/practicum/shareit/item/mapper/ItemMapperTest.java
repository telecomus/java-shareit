package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toItemDto_WithValidItem_ShouldReturnItemDto() {
        // Arrange
        User owner = new User(1L, "Owner", "owner@example.com");
        ItemRequest request = new ItemRequest(1L, "Need a drill", new User(), LocalDateTime.now());
        Item item = new Item(1L, "Drill", "Electric drill", true, owner, request);

        // Act
        ItemDto itemDto = ItemMapper.toItemDto(item);

        // Assert
        assertEquals(1L, itemDto.getId());
        assertEquals("Drill", itemDto.getName());
        assertEquals("Electric drill", itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
        assertEquals(1L, itemDto.getRequestId());
    }

    @Test
    void toItemDto_WithNullRequest_ShouldReturnItemDtoWithNullRequestId() {
        // Arrange
        User owner = new User(1L, "Owner", "owner@example.com");
        Item item = new Item(1L, "Drill", "Electric drill", true, owner, null);

        // Act
        ItemDto itemDto = ItemMapper.toItemDto(item);

        // Assert
        assertEquals(1L, itemDto.getId());
        assertEquals("Drill", itemDto.getName());
        assertEquals("Electric drill", itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
        assertNull(itemDto.getRequestId());
    }

    @Test
    void toItem_WithValidItemDto_ShouldReturnItem() {
        // Arrange
        ItemDto itemDto = new ItemDto(1L, "Drill", "Electric drill", true, 1L);

        // Act
        Item item = ItemMapper.toItem(itemDto);

        // Assert
        assertEquals(1L, item.getId());
        assertEquals("Drill", item.getName());
        assertEquals("Electric drill", item.getDescription());
        assertTrue(item.getAvailable());
        assertNull(item.getOwner()); // Owner is set separately
        assertNull(item.getRequest()); // Request is set separately
    }

    @Test
    void toItemWithBookingDto_WithValidItem_ShouldReturnItemWithBookingDto() {
        // Arrange
        User owner = new User(1L, "Owner", "owner@example.com");
        ItemRequest request = new ItemRequest(1L, "Need a drill", new User(), LocalDateTime.now());
        Item item = new Item(1L, "Drill", "Electric drill", true, owner, request);

        BookingShortDto lastBooking = new BookingShortDto(1L, 2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        BookingShortDto nextBooking = new BookingShortDto(2L, 2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        List<CommentDto> comments = new ArrayList<>();
        comments.add(new CommentDto(1L, "Great drill!", "User", LocalDateTime.now()));

        // Act
        ItemWithBookingDto itemWithBookingDto = ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking, comments);

        // Assert
        assertEquals(1L, itemWithBookingDto.getId());
        assertEquals("Drill", itemWithBookingDto.getName());
        assertEquals("Electric drill", itemWithBookingDto.getDescription());
        assertTrue(itemWithBookingDto.getAvailable());
        assertEquals(1L, itemWithBookingDto.getRequestId());
        assertEquals(lastBooking, itemWithBookingDto.getLastBooking());
        assertEquals(nextBooking, itemWithBookingDto.getNextBooking());
        assertEquals(1, itemWithBookingDto.getComments().size());
        assertEquals("Great drill!", itemWithBookingDto.getComments().get(0).getText());
    }

    @Test
    void toItemWithBookingDto_WithNullBookings_ShouldHandleNullBookings() {
        // Arrange
        User owner = new User(1L, "Owner", "owner@example.com");
        ItemRequest request = new ItemRequest(1L, "Need a drill", new User(), LocalDateTime.now());
        Item item = new Item(1L, "Drill", "Electric drill", true, owner, request);

        List<CommentDto> comments = new ArrayList<>();

        // Act
        ItemWithBookingDto itemWithBookingDto = ItemMapper.toItemWithBookingDto(item, null, null, comments);

        // Assert
        assertEquals(1L, itemWithBookingDto.getId());
        assertEquals("Drill", itemWithBookingDto.getName());
        assertEquals("Electric drill", itemWithBookingDto.getDescription());
        assertTrue(itemWithBookingDto.getAvailable());
        assertEquals(1L, itemWithBookingDto.getRequestId());
        assertNull(itemWithBookingDto.getLastBooking());
        assertNull(itemWithBookingDto.getNextBooking());
        assertEquals(0, itemWithBookingDto.getComments().size());
    }
}