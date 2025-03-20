package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.util.Constants;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@ContextConfiguration(classes = {BookingController.class})
class BookingControllerTest {

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingDto bookingDto;
    private BookingResponseDto bookingResponseDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        bookingDto = new BookingDto(
                1L,
                now.plusDays(1),
                now.plusDays(2),
                1L,
                BookingStatus.WAITING
        );

        UserDto booker = new UserDto(2L, "Booker", "booker@example.com");
        ItemDto item = new ItemDto(1L, "Drill", "Electric drill", true, null);

        bookingResponseDto = new BookingResponseDto(
                1L,
                now.plusDays(1),
                now.plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedBooking() throws Exception {
        when(bookingService.create(anyLong(), any(BookingDto.class))).thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header(Constants.USER_ID_HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.item.id", is(1)));

        verify(bookingService, times(1)).create(anyLong(), any(BookingDto.class));
    }

    @Test
    void create_WithoutUserHeader_ShouldFail() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).create(anyLong(), any(BookingDto.class));
    }

    @Test
    void approve_WithValidData_ShouldReturnApprovedBooking() throws Exception {
        BookingResponseDto approvedBooking = new BookingResponseDto(
                1L,
                now.plusDays(1),
                now.plusDays(2),
                bookingResponseDto.getItem(),
                bookingResponseDto.getBooker(),
                BookingStatus.APPROVED
        );

        when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header(Constants.USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(bookingService, times(1)).approve(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void getById_ByAuthorizedUser_ShouldReturnBooking() throws Exception {
        when(bookingService.getById(anyLong(), anyLong())).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/1")
                        .header(Constants.USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(bookingService, times(1)).getById(anyLong(), anyLong());
    }

    @Test
    void getAllByBooker_ShouldReturnBookingsList() throws Exception {
        when(bookingService.getAllByBooker(anyLong(), any(BookingState.class)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header(Constants.USER_ID_HEADER, 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("WAITING")));

        verify(bookingService, times(1)).getAllByBooker(anyLong(), any(BookingState.class));
    }

    @Test
    void getAllByOwner_ShouldReturnBookingsList() throws Exception {
        when(bookingService.getAllByOwner(anyLong(), any(BookingState.class)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(Constants.USER_ID_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("WAITING")));

        verify(bookingService, times(1)).getAllByOwner(anyLong(), any(BookingState.class));
    }

    @Test
    void parseBookingState_WithValidState_ShouldReturnState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(Constants.USER_ID_HEADER, 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getAllByBooker(anyLong(), eq(BookingState.ALL));
    }

}