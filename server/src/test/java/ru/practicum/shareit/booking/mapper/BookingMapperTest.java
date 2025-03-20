package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookingMapperTest {

    @Test
    void toBookingResponseDto_WithValidBooking_ShouldReturnBookingResponseDto() {
        // Arrange
        User owner = new User(1L, "Owner", "owner@example.com");
        User booker = new User(2L, "Booker", "booker@example.com");
        Item item = new Item(1L, "Drill", "Electric drill", true, owner, null);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Booking booking = new Booking(1L, start, end, item, booker, BookingStatus.WAITING);

        // Act
        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(booking);

        // Assert
        assertEquals(1L, bookingResponseDto.getId());
        assertEquals(start, bookingResponseDto.getStart());
        assertEquals(end, bookingResponseDto.getEnd());
        assertEquals(1L, bookingResponseDto.getItem().getId());
        assertEquals("Drill", bookingResponseDto.getItem().getName());
        assertEquals(2L, bookingResponseDto.getBooker().getId());
        assertEquals("Booker", bookingResponseDto.getBooker().getName());
        assertEquals(BookingStatus.WAITING, bookingResponseDto.getStatus());
    }

    @Test
    void toBookingResponseDto_WithNullBooking_ShouldHandleNullSafely() {
        // Act
        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(null);

        // Assert
        assertNull(bookingResponseDto);
    }

    @Test
    void toBooking_WithValidBookingDto_ShouldReturnBooking() {
        // Arrange
        User owner = new User(1L, "Owner", "owner@example.com");
        User booker = new User(2L, "Booker", "booker@example.com");
        Item item = new Item(1L, "Drill", "Electric drill", true, owner, null);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingDto bookingDto = new BookingDto(1L, start, end, 1L, BookingStatus.WAITING);

        // Act
        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);

        // Assert
        assertEquals(1L, booking.getId());
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    void toBooking_WithNullBookingDto_ShouldHandleNullSafely() {
        // Arrange
        User booker = new User(2L, "Booker", "booker@example.com");
        Item item = new Item(1L, "Drill", "Electric drill", true, new User(), null);

        // Act
        Booking booking = BookingMapper.toBooking(null, item, booker);

        // Assert
        assertNull(booking);
    }

    @Test
    void toBookingShortDto_WithValidBooking_ShouldReturnBookingShortDto() {
        // Arrange
        User owner = new User(1L, "Owner", "owner@example.com");
        User booker = new User(2L, "Booker", "booker@example.com");
        Item item = new Item(1L, "Drill", "Electric drill", true, owner, null);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Booking booking = new Booking(1L, start, end, item, booker, BookingStatus.WAITING);

        // Act
        BookingShortDto bookingShortDto = BookingMapper.toBookingShortDto(booking);

        // Assert
        assertEquals(1L, bookingShortDto.getId());
        assertEquals(2L, bookingShortDto.getBookerId());
        assertEquals(start, bookingShortDto.getStart());
        assertEquals(end, bookingShortDto.getEnd());
    }

    @Test
    void toBookingShortDto_WithNullBooking_ShouldHandleNullSafely() {
        // Act
        BookingShortDto bookingShortDto = BookingMapper.toBookingShortDto(null);

        // Assert
        assertNull(bookingShortDto);
    }
}