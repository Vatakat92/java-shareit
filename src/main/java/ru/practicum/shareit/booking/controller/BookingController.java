package ru.practicum.shareit.booking.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.validation.Create;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                            @Validated(Create.class) @RequestBody BookingDto bookingDto) {
        log.info("Booking API create: userId={} itemId={}", userId, bookingDto.getItemId());
        BookingResponseDto created = bookingService.createBooking(userId, bookingDto);
        URI location = URI.create("/bookings/" + created.getId());
        log.info("Booking API created: id={} itemId={}", created.getId(), created.getItem() != null ? created.getItem().getId() : "null");
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @PathVariable Long bookingId,
                                                             @RequestParam Boolean approved) {
        log.info("Booking API approve: userId={} bookingId={}", userId, bookingId);
        BookingResponseDto updated = bookingService.approveBooking(userId, bookingId, approved);
        log.info("Booking API approved: id={} status={}", updated.getId(), updated.getStatus());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @PathVariable Long bookingId) {
        log.debug("Booking API get by id: userId={} bookingId={}", userId, bookingId);
        BookingResponseDto booking = bookingService.getBooking(userId, bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                    @RequestParam(defaultValue = "ALL") String state,
                                                                    @RequestParam(defaultValue = "0") @Min(0) int from,
                                                                    @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.debug("Booking API list user bookings: userId={} state={} from={} size={}", userId, state, from, size);
        List<BookingResponseDto> bookings = bookingService.getUserBookings(userId, state, from, size);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                     @RequestParam(defaultValue = "ALL") String state,
                                                                     @RequestParam(defaultValue = "0") @Min(0) int from,
                                                                     @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.debug("Booking API list owner bookings: userId={} state={} from={} size={}", userId, state, from, size);
        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(userId, state, from, size);
        return ResponseEntity.ok(bookings);
    }
}