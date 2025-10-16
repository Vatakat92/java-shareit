package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> bookItem(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@Valid @RequestBody BookingCreateDto requestDto) {
		log.info("Gateway → create booking {}, userId={}", requestDto, userId);
		if (userId <= 0) {
			log.warn("Invalid user ID: {}", userId);
			return ResponseEntity.badRequest().body("Invalid user ID");
		}
		return bookingClient.bookItem(userId, requestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approveBooking(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@PathVariable Long bookingId,
			@RequestParam Boolean approved) {
		log.info("Gateway → approve booking {}, userId={}, approved={}", bookingId, userId, approved);
		return bookingClient.approveBooking(userId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@PathVariable Long bookingId) {
		log.info("Gateway → get booking {}, userId={}", bookingId, userId);
		if (userId <= 0) {
			log.warn("Invalid user ID: {}", userId);
			return ResponseEntity.badRequest().body("Invalid user ID");
		}
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getBookings(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(defaultValue = "ALL") String state,
			@RequestParam(defaultValue = "0") @Min(0) int from,
			@RequestParam(defaultValue = "10") @Min(1) int size) {
		log.info("Gateway → list bookings, userId={}, state={}, from={}, size={}", userId, state, from, size);
		if (userId <= 0) {
			log.warn("Invalid user ID: {}", userId);
			return ResponseEntity.badRequest().body("Invalid user ID");
		}
        BookingState bookingState = BookingState.from(state)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
		return bookingClient.getBookings(userId, bookingState, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getOwnerBookings(
			@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(defaultValue = "ALL") String state,
			@RequestParam(defaultValue = "0") @Min(0) int from,
			@RequestParam(defaultValue = "10") @Min(1) int size) {
		log.info("Gateway → list owner bookings, userId={}, state={}, from={}, size={}", userId, state, from, size);
		if (userId <= 0) {
			log.warn("Invalid user ID: {}", userId);
			return ResponseEntity.badRequest().body("Invalid user ID");
		}
        BookingState bookingState = BookingState.from(state)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
		return bookingClient.getOwnerBookings(userId, bookingState, from, size);
	}
}