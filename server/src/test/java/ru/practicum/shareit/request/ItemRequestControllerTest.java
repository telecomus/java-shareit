package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
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

@WebMvcTest(ItemRequestController.class)
@ContextConfiguration(classes = {ItemRequestController.class})
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemRequestDto itemRequestDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        ItemDto itemDto = new ItemDto(1L, "Drill", "Electric drill", true, 1L);

        itemRequestDto = new ItemRequestDto(
                1L,
                "Need a drill",
                1L,
                now,
                List.of(itemDto)
        );
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedItemRequest() throws Exception {
        when(itemRequestService.create(anyLong(), any(ItemRequestDto.class))).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header(Constants.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need a drill")))
                .andExpect(jsonPath("$.requestorId", is(1)))
                .andExpect(jsonPath("$.items", hasSize(1)));

        verify(itemRequestService, times(1)).create(anyLong(), any(ItemRequestDto.class));
    }

    @Test
    void create_WithoutUserHeader_ShouldFail() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).create(anyLong(), any(ItemRequestDto.class));
    }

    @Test
    void getAllByRequestor_ShouldReturnListOfItemRequests() throws Exception {
        when(itemRequestService.getAllByRequestor(anyLong())).thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests")
                        .header(Constants.USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Need a drill")));

        verify(itemRequestService, times(1)).getAllByRequestor(anyLong());
    }

    @Test
    void getAll_ShouldReturnListOfItemRequests() throws Exception {
        when(itemRequestService.getAll(anyLong())).thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests/all")
                        .header(Constants.USER_ID_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Need a drill")));

        verify(itemRequestService, times(1)).getAll(anyLong());
    }

    @Test
    void getById_WithExistingItemRequest_ShouldReturnItemRequest() throws Exception {
        when(itemRequestService.getById(anyLong(), anyLong())).thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/1")
                        .header(Constants.USER_ID_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need a drill")))
                .andExpect(jsonPath("$.items", hasSize(1)));

        verify(itemRequestService, times(1)).getById(anyLong(), anyLong());
    }

}