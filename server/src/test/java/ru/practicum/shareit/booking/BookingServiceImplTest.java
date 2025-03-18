package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        owner = new User(1L, "Owner", "owner@example.com");
        booker = new User(2L, "Booker", "booker@example.com");

        item = new Item(1L, "Drill", "Electric drill", true, owner, null);

        bookingDto = new BookingDto(
                1L,
                now.plusDays(1),
                now.plusDays(2),
                1L,
                BookingStatus.WAITING
        );

        booking = new Booking(
                1L,
                now.plusDays(1),
                now.plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );
    }

    @Test
    void create_WithValidData_ShouldReturnBookingResponseDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.create(2L, bookingDto);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(BookingStatus.WAITING, result.getStatus());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void create_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(2L, bookingDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_WithNonExistingItem_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(2L, bookingDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_ByOwner_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.create(1L, bookingDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_WithUnavailableItem_ShouldThrowValidationException() {
        Item unavailableItem = new Item(1L, "Drill", "Electric drill", false, owner, null);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(unavailableItem));

        assertThrows(ValidationException.class, () -> bookingService.create(2L, bookingDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_WithInvalidDates_ShouldThrowValidationException() {
        BookingDto invalidBookingDto = new BookingDto(
                1L,
                now.plusDays(2),
                now.plusDays(1),
                1L,
                BookingStatus.WAITING
        );

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(2L, invalidBookingDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approve_WithValidData_ShouldReturnApprovedBookingResponseDto() {
        Booking approvedBooking = new Booking(
                1L,
                now.plusDays(1),
                now.plusDays(2),
                item,
                booker,
                BookingStatus.APPROVED
        );

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(approvedBooking);

        BookingResponseDto result = bookingService.approve(1L, 1L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());

        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void approve_WithValidData_ShouldReturnRejectedBookingResponseDto() {
        Booking rejectedBooking = new Booking(
                1L,
                now.plusDays(1),
                now.plusDays(2),
                item,
                booker,
                BookingStatus.REJECTED
        );

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(rejectedBooking);

        BookingResponseDto result = bookingService.approve(1L, 1L, false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());

        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void approve_WithNonExistingBooking_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.approve(1L, 1L, true));

        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approve_ByNonOwner_ShouldThrowForbiddenException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class, () -> bookingService.approve(2L, 1L, true));

        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approve_AlreadyApprovedBooking_ShouldThrowValidationException() {
        Booking approvedBooking = new Booking(
                1L,
                now.plusDays(1),
                now.plusDays(2),
                item,
                booker,
                BookingStatus.APPROVED
        );

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(approvedBooking));

        assertThrows(ValidationException.class, () -> bookingService.approve(1L, 1L, true));

        verify(bookingRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getById_ByOwner_ShouldReturnBookingResponseDto() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getById(1L, 1L);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void getById_ByBooker_ShouldReturnBookingResponseDto() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getById(2L, 1L);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void getById_ByUnauthorizedUser_ShouldThrowNotFoundException() {
        User unauthorizedUser = new User(3L, "Stranger", "stranger@example.com");

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getById(3L, 1L));

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void getById_WithNonExistingBooking_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getById(1L, 1L));

        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void getAllByBooker_WithStateAll_ShouldReturnAllBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerId(anyLong(), any(Sort.class))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBooker(2L, BookingState.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByBookerId(anyLong(), any(Sort.class));
    }

    @Test
    void getAllByBooker_WithStateCurrent_ShouldReturnCurrentBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findCurrentBookingsByBookerId(anyLong(), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBooker(2L, BookingState.CURRENT);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findCurrentBookingsByBookerId(anyLong(), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllByBooker_WithStatePast_ShouldReturnPastBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndEndIsBefore(anyLong(), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBooker(2L, BookingState.PAST);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByBookerIdAndEndIsBefore(anyLong(), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllByBooker_WithStateFuture_ShouldReturnFutureBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartIsAfter(anyLong(), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBooker(2L, BookingState.FUTURE);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByBookerIdAndStartIsAfter(anyLong(), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllByBooker_WithStateWaiting_ShouldReturnWaitingBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatus(anyLong(), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBooker(2L, BookingState.WAITING);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByBookerIdAndStatus(anyLong(), eq(BookingStatus.WAITING), any(Sort.class));
    }

    @Test
    void getAllByBooker_WithStateRejected_ShouldReturnRejectedBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatus(anyLong(), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByBooker(2L, BookingState.REJECTED);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findByBookerIdAndStatus(anyLong(), eq(BookingStatus.REJECTED), any(Sort.class));
    }

    @Test
    void getAllByBooker_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getAllByBooker(2L, BookingState.ALL));

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getAllByOwner_WithStateAll_ShouldReturnAllBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findByOwnerId(anyLong(), any(Sort.class))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwner(1L, BookingState.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findByOwnerId(anyLong());
        verify(bookingRepository, times(1)).findByOwnerId(anyLong(), any(Sort.class));
    }

    @Test
    void getAllByOwner_WithStateCurrent_ShouldReturnCurrentBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findCurrentBookingsByOwnerId(anyLong(), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwner(1L, BookingState.CURRENT);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findByOwnerId(anyLong());
        verify(bookingRepository, times(1)).findCurrentBookingsByOwnerId(anyLong(), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllByOwner_WithStatePast_ShouldReturnPastBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findPastBookingsByOwnerId(anyLong(), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwner(1L, BookingState.PAST);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findByOwnerId(anyLong());
        verify(bookingRepository, times(1)).findPastBookingsByOwnerId(anyLong(), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllByOwner_WithStateFuture_ShouldReturnFutureBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findFutureBookingsByOwnerId(anyLong(), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwner(1L, BookingState.FUTURE);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findByOwnerId(anyLong());
        verify(bookingRepository, times(1)).findFutureBookingsByOwnerId(anyLong(), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getAllByOwner_WithStateWaiting_ShouldReturnWaitingBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findByOwnerIdAndStatus(anyLong(), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwner(1L, BookingState.WAITING);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findByOwnerId(anyLong());
        verify(bookingRepository, times(1)).findByOwnerIdAndStatus(anyLong(), eq(BookingStatus.WAITING), any(Sort.class));
    }

    @Test
    void getAllByOwner_WithStateRejected_ShouldReturnRejectedBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(List.of(item));
        when(bookingRepository.findByOwnerIdAndStatus(anyLong(), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getAllByOwner(1L, BookingState.REJECTED);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findByOwnerId(anyLong());
        verify(bookingRepository, times(1)).findByOwnerIdAndStatus(anyLong(), eq(BookingStatus.REJECTED), any(Sort.class));
    }

    @Test
    void getAllByOwner_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getAllByOwner(1L, BookingState.ALL));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findByOwnerId(anyLong());
    }

    @Test
    void getAllByOwner_WithNoItems_ShouldReturnEmptyList() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(List.of());

        List<BookingResponseDto> result = bookingService.getAllByOwner(1L, BookingState.ALL);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findByOwnerId(anyLong());
        verify(bookingRepository, never()).findByOwnerId(anyLong(), any(Sort.class));
    }
}