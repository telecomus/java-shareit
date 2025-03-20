package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.util.Constants;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@ContextConfiguration(classes = {ItemController.class})
class ItemControllerTest {

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemDto itemDto;
    private ItemWithBookingDto itemWithBookingDto;
    private CommentDto commentDto;
    private CommentRequestDto commentRequestDto;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto(1L, "Drill", "Electric drill", true, null);

        commentDto = new CommentDto(1L, "Great drill!", "User", LocalDateTime.now());

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Great drill!");

        BookingShortDto lastBooking = new BookingShortDto(1L, 2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        BookingShortDto nextBooking = new BookingShortDto(2L, 2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        itemWithBookingDto = new ItemWithBookingDto(
                1L,
                "Drill",
                "Electric drill",
                true,
                null,
                lastBooking,
                nextBooking,
                List.of(commentDto)
        );
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedItem() throws Exception {
        when(itemService.create(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(Constants.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")))
                .andExpect(jsonPath("$.description", is("Electric drill")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemService, times(1)).create(anyLong(), any(ItemDto.class));
    }

    @Test
    void create_WithoutUserHeader_ShouldFail() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).create(anyLong(), any(ItemDto.class));
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedItem() throws Exception {
        ItemDto updateDto = new ItemDto(1L, "Updated Drill", "Updated description", false, null);
        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(updateDto);

        mockMvc.perform(patch("/items/1")
                        .header(Constants.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Drill")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.available", is(false)));

        verify(itemService, times(1)).update(anyLong(), anyLong(), any(ItemDto.class));
    }

    @Test
    void getById_WithExistingItem_ShouldReturnItem() throws Exception {
        when(itemService.getById(anyLong(), anyLong())).thenReturn(itemWithBookingDto);

        mockMvc.perform(get("/items/1")
                        .header(Constants.USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")))
                .andExpect(jsonPath("$.description", is("Electric drill")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.lastBooking").exists())
                .andExpect(jsonPath("$.nextBooking").exists())
                .andExpect(jsonPath("$.comments", hasSize(1)));

        verify(itemService, times(1)).getById(anyLong(), anyLong());
    }

    @Test
    void getAllByUserId_ShouldReturnListOfItems() throws Exception {
        when(itemService.getAllByUserId(anyLong())).thenReturn(List.of(itemWithBookingDto));

        mockMvc.perform(get("/items")
                        .header(Constants.USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")));

        verify(itemService, times(1)).getAllByUserId(anyLong());
    }

    @Test
    void search_WithValidText_ShouldReturnMatchingItems() throws Exception {
        when(itemService.search(anyString())).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill")
                        .header(Constants.USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")));

        verify(itemService, times(1)).search(anyString());
    }

    @Test
    void search_WithEmptyText_ShouldReturnEmptyList() throws Exception {
        when(itemService.search(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .header(Constants.USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemService, times(1)).search(anyString());
    }

    @Test
    void createComment_WithValidData_ShouldReturnCreatedComment() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), any(CommentRequestDto.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(Constants.USER_ID_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Great drill!")))
                .andExpect(jsonPath("$.authorName", is("User")));

        verify(itemService, times(1)).createComment(anyLong(), anyLong(), any(CommentRequestDto.class));
    }

}