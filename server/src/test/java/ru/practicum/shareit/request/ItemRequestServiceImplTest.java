package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requestor;
    private User owner;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        requestor = new User(1L, "Requestor", "requestor@example.com");
        owner = new User(2L, "Owner", "owner@example.com");

        itemRequest = new ItemRequest(1L, "Need a drill", requestor, now);

        itemRequestDto = new ItemRequestDto(1L, "Need a drill", 1L, now, Collections.emptyList());

        item = new Item(1L, "Drill", "Electric drill", true, owner, itemRequest);
    }

    @Test
    void create_WithValidData_ShouldReturnItemRequestDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.create(1L, itemRequestDto);

        assertNotNull(result);
        assertEquals(itemRequestDto.getId(), result.getId());
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        assertEquals(itemRequestDto.getRequestorId(), result.getRequestorId());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void create_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.create(1L, itemRequestDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void create_WithEmptyDescription_ShouldThrowValidationException() {
        ItemRequestDto invalidDto = new ItemRequestDto(1L, "", 1L, now, Collections.emptyList());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requestor));

        assertThrows(ValidationException.class, () -> itemRequestService.create(1L, invalidDto));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void getAllByRequestor_WithExistingUser_ShouldReturnListOfItemRequestDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(requestor));
        when(itemRequestRepository.findByRequestorId(anyLong(), any(Sort.class))).thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getAllByRequestor(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemRequest.getId(), result.get(0).getId());
        assertEquals(itemRequest.getDescription(), result.get(0).getDescription());
        assertEquals(1, result.get(0).getItems().size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findByRequestorId(anyLong(), any(Sort.class));
        verify(itemRepository, times(1)).findByRequestIdIn(anyList());
    }

    @Test
    void getAllByRequestor_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getAllByRequestor(1L));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).findByRequestorId(anyLong(), any(Sort.class));
    }

    @Test
    void getAll_WithExistingUser_ShouldReturnListOfItemRequestDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findByRequestorIdNot(anyLong(), any(Sort.class))).thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getAll(2L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(itemRequest.getId(), result.get(0).getId());
        assertEquals(itemRequest.getDescription(), result.get(0).getDescription());
        assertEquals(1, result.get(0).getItems().size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findByRequestorIdNot(anyLong(), any(Sort.class));
        verify(itemRepository, times(1)).findByRequestIdIn(anyList());
    }

    @Test
    void getAll_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getAll(2L));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).findByRequestorIdNot(anyLong(), any(Sort.class));
    }

    @Test
    void getById_WithExistingUserAndRequest_ShouldReturnItemRequestDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getById(1L, 2L);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(1, result.getItems().size());

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findByRequestId(anyLong());
    }

    @Test
    void getById_WithNonExistingUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getById(1L, 2L));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).findById(anyLong());
    }

    @Test
    void getById_WithNonExistingRequest_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getById(1L, 2L));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findByRequestId(anyLong());
    }
}