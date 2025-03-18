package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.util.Constants;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(@RequestHeader(Constants.USER_ID_HEADER) long userId,
                                     @RequestBody BookingDto bookingDto) {
        log.info("Получен запрос на создание бронирования пользователем с id {}", userId);
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@RequestHeader(Constants.USER_ID_HEADER) long userId,
                                      @PathVariable long bookingId,
                                      @RequestParam boolean approved) {
        log.info("Получен запрос на подтверждение/отклонение бронирования с id {} пользователем с id {}, статус: {}",
                bookingId, userId, approved ? "одобрено" : "отклонено");
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@RequestHeader(Constants.USER_ID_HEADER) long userId,
                                      @PathVariable long bookingId) {
        log.info("Получен запрос на получение данных о бронировании с id {} пользователем с id {}", bookingId, userId);
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBooker(@RequestHeader(Constants.USER_ID_HEADER) long userId,
                                                   @RequestParam(defaultValue = "ALL") String state) {
        BookingState bookingState = parseBookingState(state);
        log.info("Получен запрос на получение списка бронирований пользователя с id {} в статусе {}", userId, bookingState);
        return bookingService.getAllByBooker(userId, bookingState);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(@RequestHeader(Constants.USER_ID_HEADER) long userId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        BookingState bookingState = parseBookingState(state);
        log.info("Получен запрос на получение списка бронирований вещей владельца с id {} в статусе {}", userId, bookingState);
        return bookingService.getAllByOwner(userId, bookingState);
    }

    private BookingState parseBookingState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ru.practicum.shareit.exception.ValidationException("Unknown state: " + state);
        }
    }
}