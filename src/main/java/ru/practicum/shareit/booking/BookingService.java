package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto create(long userId, BookingDto bookingDto);

    BookingResponseDto approve(long userId, long bookingId, boolean approved);

    BookingResponseDto getById(long userId, long bookingId);

    List<BookingResponseDto> getAllByBooker(long userId, BookingState state);

    List<BookingResponseDto> getAllByOwner(long userId, BookingState state);
}