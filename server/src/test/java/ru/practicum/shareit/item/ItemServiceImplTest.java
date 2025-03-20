package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;
    private ItemRequest itemRequest;
    private Comment comment;
    private CommentRequestDto commentRequestDto;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@example.com");
        booker = new User(2L, "Booker", "booker@example.com");
        itemRequest = new ItemRequest(1L, "Need a drill", booker, LocalDateTime.now());

        item = new Item(1L, "Drill", "Electric drill", true, owner, itemRequest);

        itemDto = new ItemDto(1L, "Drill", "Electric drill", true, 1L);

        comment = new Comment(1L, "Great drill!", item, booker, LocalDateTime.now());

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Great drill!");

        LocalDateTime now = LocalDateTime.now();

        lastBooking = new Booking(1L,
                now.minusDays(2),
                now.minusDays(1),
                item,
                booker,
                BookingStatus.APPROVED);

        nextBooking = new Booking(2L,
                now.plusDays(1),
                now.plusDays(2),
                item,
                booker,
                BookingStatus.APPROVED);
    }

    @Test
    void create_WithValidData_ShouldReturnItemDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(1L, itemDto);

        assertNotNull(result);
        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.getAvailable());
        assertEquals(itemDto.getRequestId(), result.getRequestId());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void create_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(1L, itemDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void create_WithNonExistingRequest_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.create(1L, itemDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedItemDto() {
        ItemDto updateDto = new ItemDto(1L, "Updated Drill", "Updated description", false, null);
        Item updatedItem = new Item(1L, "Updated Drill", "Updated description", false, owner, itemRequest);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals(updateDto.getName(), result.getName());
        assertEquals(updateDto.getDescription(), result.getDescription());
        assertEquals(updateDto.getAvailable(), result.getAvailable());

        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void update_WithNonExistingItem_ShouldThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.update(1L, 1L, itemDto));

        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void update_ByNonOwner_ShouldThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.update(2L, 1L, itemDto));

        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void update_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        ItemDto updateDto = new ItemDto(null, null, "Updated description", null, null);
        Item originalItem = new Item(1L, "Drill", "Electric drill", true, owner, itemRequest);
        Item updatedItem = new Item(1L, "Drill", "Updated description", true, owner, itemRequest);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(originalItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        ItemDto result = itemService.update(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Drill", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertTrue(result.getAvailable());

        verify(itemRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void getById_ByOwner_ShouldReturnItemWithBookingsInfo() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBookingForItem(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findNextBookingForItem(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(nextBooking));
        when(commentRepository.findByItemId(anyLong())).thenReturn(List.of(comment));

        ItemWithBookingDto result = itemService.getById(1L, 1L);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertEquals(1, result.getComments().size());

        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).findLastBookingForItem(anyLong(), any(LocalDateTime.class));
        verify(bookingRepository, times(1)).findNextBookingForItem(anyLong(), any(LocalDateTime.class));
        verify(commentRepository, times(1)).findByItemId(anyLong());
    }

    @Test
    void getById_ByNonOwner_ShouldReturnItemWithoutBookingsInfo() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(anyLong())).thenReturn(List.of(comment));

        ItemWithBookingDto result = itemService.getById(1L, 2L);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertEquals(1, result.getComments().size());

        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, never()).findLastBookingForItem(anyLong(), any(LocalDateTime.class));
        verify(bookingRepository, never()).findNextBookingForItem(anyLong(), any(LocalDateTime.class));
        verify(commentRepository, times(1)).findByItemId(anyLong());
    }

    @Test
    void getById_WithNonExistingItem_ShouldThrowNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(1L, 1L));

        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void getAllByUserId_ShouldReturnListOfItemWithBookingDto() {
        List<Item> items = List.of(item);
        List<Comment> comments = List.of(comment);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(anyLong())).thenReturn(items);
        when(commentRepository.findByItemIdIn(anyList())).thenReturn(comments);
        when(bookingRepository.findLastBookingForItem(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findNextBookingForItem(anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(nextBooking));

        List<ItemWithBookingDto> result = itemService.getAllByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getLastBooking());
        assertNotNull(result.get(0).getNextBooking());
        assertEquals(1, result.get(0).getComments().size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findByOwnerId(anyLong());
        verify(commentRepository, times(1)).findByItemIdIn(anyList());
        verify(bookingRepository, times(items.size())).findLastBookingForItem(anyLong(), any(LocalDateTime.class));
        verify(bookingRepository, times(items.size())).findNextBookingForItem(anyLong(), any(LocalDateTime.class));
    }

    @Test
    void getAllByUserId_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getAllByUserId(1L));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findByOwnerId(anyLong());
    }

    @Test
    void search_WithValidText_ShouldReturnMatchingItems() {
        when(itemRepository.search(anyString())).thenReturn(List.of(item));

        List<ItemDto> result = itemService.search("drill");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());

        verify(itemRepository, times(1)).search(anyString());
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.search("");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(itemRepository, never()).search(anyString());
    }

    @Test
    void createComment_WithValidData_ShouldReturnCommentDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.hasUserBookedItem(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.createComment(2L, 1L, commentRequestDto);

        assertNotNull(result);
        assertEquals(comment.getText(), result.getText());
        assertEquals(booker.getName(), result.getAuthorName());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).hasUserBookedItem(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void createComment_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createComment(2L, 1L, commentRequestDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void createComment_WithNonExistingItem_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createComment(2L, 1L, commentRequestDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
    }

    @Test
    void createComment_WithoutBooking_ShouldThrowValidationException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.hasUserBookedItem(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.createComment(2L, 1L, commentRequestDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).hasUserBookedItem(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_WithEmptyText_ShouldThrowValidationException() {
        CommentRequestDto emptyCommentDto = new CommentRequestDto();
        emptyCommentDto.setText("");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.hasUserBookedItem(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(true);

        assertThrows(ValidationException.class, () -> itemService.createComment(2L, 1L, emptyCommentDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findById(anyLong());
        verify(bookingRepository, times(1)).hasUserBookedItem(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepository, never()).save(any(Comment.class));
    }
}