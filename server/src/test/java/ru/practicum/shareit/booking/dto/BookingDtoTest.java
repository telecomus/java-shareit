package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItServer.class)
@ActiveProfiles("test")
public class BookingDtoTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testBookingDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 12, 0);
        LocalDateTime end = LocalDateTime.of(2023, 1, 2, 12, 0);

        BookingDto bookingDto = new BookingDto(
                1L,
                start,
                end,
                1L,
                BookingStatus.WAITING
        );

        var jsonContent = json.write(bookingDto).getJson();

        assertThat(jsonContent).contains("\"id\":1");
        assertThat(jsonContent).contains("\"itemId\":1");
        assertThat(jsonContent).contains("\"status\":\"WAITING\"");
    }

    @Test
    void testBookingDtoDeserialization() throws Exception {
        String jsonContent = "{\"id\":1,\"start\":\"2023-01-01T12:00:00\",\"end\":\"2023-01-02T12:00:00\",\"itemId\":1,\"status\":\"WAITING\"}";

        BookingDto result = json.parse(jsonContent).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2023, 1, 2, 12, 0, 0));
        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}