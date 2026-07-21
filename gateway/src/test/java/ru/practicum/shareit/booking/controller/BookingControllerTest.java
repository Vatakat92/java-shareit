
package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.exception.GatewayExceptionHandler;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private BookingController bookingController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .setControllerAdvice(new GatewayExceptionHandler()) // Добавляем обработчик исключений
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Для работы с LocalDateTime
    }

    @Test
    void createBooking_WhenValidData_ShouldReturnOk() throws Exception {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();

        when(bookingClient.bookItem(anyLong(), any(BookingCreateDto.class))).thenReturn(responseEntity);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WhenStartInPast_ShouldReturnBadRequest() throws Exception {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().minusDays(1)); // Past date
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_WhenEndNotInFuture_ShouldReturnBadRequest() throws Exception {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().minusDays(1)); // Past date

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_WhenValidData_ShouldReturnOk() throws Exception {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();

        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(responseEntity);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void updateBookingStatus_WhenMissingApprovedParam_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_WhenValidData_ShouldReturnOk() throws Exception {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();

        when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(responseEntity);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getUserBookings_WhenValidData_ShouldReturnOk() throws Exception {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();

        when(bookingClient.getBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserBookings_WhenDifferentState_ShouldPassCorrectState() throws Exception {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();

        when(bookingClient.getBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "WAITING")
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(bookingClient).getBookings(1L, BookingState.WAITING, 5, 20);
    }

    @Test
    void getUserBookings_WhenInvalidState_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID_STATE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_WhenValidData_ShouldReturnOk() throws Exception {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();

        when(bookingClient.getOwnerBookings(anyLong(), any(BookingState.class), anyInt(), anyInt())).thenReturn(responseEntity);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_WhenDefaultParameters_ShouldUseDefaults() throws Exception {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();

        when(bookingClient.getOwnerBookings(anyLong(), any(BookingState.class), anyInt(), anyInt())).thenReturn(responseEntity);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(bookingClient).getOwnerBookings(1L, BookingState.ALL, 0, 10);
    }

    @Test
    void createBooking_WhenMissingUserIdHeader_ShouldReturnBadRequest() throws Exception {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_WhenMissingUserIdHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_WhenMissingUserIdHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserBookings_WhenAllStates_ShouldReturnOk() throws Exception {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();

        when(bookingClient.getBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        String[] states = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};
        for (String state : states) {
            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", 1L)
                            .param("state", state)
                            .param("from", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void getOwnerBookings_WhenAllStates_ShouldReturnOk() throws Exception {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();

        when(bookingClient.getOwnerBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        String[] states = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"};
        for (String state : states) {
            mockMvc.perform(get("/bookings/owner")
                            .header("X-Sharer-User-Id", 1L)
                            .param("state", state)
                            .param("from", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }
    }
}
