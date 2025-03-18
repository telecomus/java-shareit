package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@example.com");
        owner = new User(2L, "Owner", "owner@example.com");
        item = new Item(1L, "Item", "Description", true, owner, null);

        booking = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                user,
                BookingStatus.WAITING
        );

        bookingDto = new BookingDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L,
                null
        );
    }

    @Test
    void createBookingTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.create(user.getId(), bookingDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
        assertThat(result.getStatus()).isEqualTo(booking.getStatus());
    }

    @Test
    void createBookingWithUnavailableItemTest() {
        item.setAvailable(false);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(user.getId(), bookingDto));
    }

    @Test
    void approveBookingTest() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveBookingByNonOwnerTest() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class, () -> bookingService.approve(user.getId(), booking.getId(), true));
    }

    @Test
    void getBookingByIdTest() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getById(user.getId(), booking.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingByWrongUserTest() {
        User anotherUser = new User(3L, "Another", "another@example.com");

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getById(anotherUser.getId(), booking.getId()));
    }

    @Test
    void getAllBookerBookingsTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBooker(user.getId(), BookingState.ALL);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    void getAllOwnerBookingsTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findByOwnerId(anyLong(), any())).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwner(owner.getId(), BookingState.ALL);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getId()).isEqualTo(booking.getId());
    }
}