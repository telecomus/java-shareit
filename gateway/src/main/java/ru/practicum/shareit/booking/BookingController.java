package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RestController
@RequestMapping("/bookings")
    public class BookingController {
    private final String bookingIdPath = "/{bookingId}";
    private final String ownerIdPath = "/owner";
    private final String userIdHeader = "X-Sharer-User-Id";
    private final BookingClient bookingClient;

    @Autowired
    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping()
    public ResponseEntity<Object> createBooking(@RequestBody BookingRequestDto bookingRequestDto,
                                                @RequestHeader(value = userIdHeader, required = false) Long bookerId) {
        return bookingClient.createBooking(bookerId, bookingRequestDto);
    }

    @PatchMapping(bookingIdPath)
    public ResponseEntity<Object> approveBooking(@PathVariable Long bookingId,
                                                 @RequestParam Boolean approved,
                                                 @RequestHeader(value = userIdHeader, required = false) Long ownerId) {
        return bookingClient.approveBooking(bookingId, approved, ownerId);
    }

    @GetMapping(bookingIdPath)
    public ResponseEntity<Object> findBooking(@PathVariable Long bookingId,
                                              @RequestHeader(value = userIdHeader, required = false) Long bookerOrOwnerId) {
        return bookingClient.findBooking(bookerOrOwnerId, bookingId);
    }

    @GetMapping()
    public ResponseEntity<Object> findBookerBookings(@RequestParam(defaultValue = "ALL") String stateParam,
                                                     @RequestHeader(value = userIdHeader, required = false) Long userId,
                                                     @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                     @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.findBookerBookings(userId, state, from, size);
    }

    @GetMapping(ownerIdPath)
    public ResponseEntity<Object> findOwnerBookings(@RequestParam(defaultValue = "ALL") String stateParam,
                                                    @RequestHeader(value = userIdHeader, required = false) Long userId,
                                                    @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                    @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.findOwnerBookings(userId, state, from, size);
    }
}


