package ru.practicum.shareit.booking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookingCreateDtoValidationTestGateway {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void bookingDto_WhenValidData_ShouldPassValidation() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(bookingDto);

        assertTrue(violations.isEmpty(), "Validation should pass successfully");
    }

    @Test
    void bookingDto_WhenItemIdIsNull_ShouldFailValidation() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(null);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty(), "There should be validation violations");
        assertEquals(1, violations.size(), "There should be exactly one violation");
        assertEquals("Item ID is required", violations.iterator().next().getMessage(),
                "The error message should be correct");
        assertEquals("itemId", violations.iterator().next().getPropertyPath().toString(),
                "The violation should be for the itemId field");
    }

    @Test
    void bookingDto_WhenStartIsInPast_ShouldFailValidation() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty(), "There should be validation violations");
        assertEquals(1, violations.size(), "There should be exactly one violation");
        assertEquals("Start date must be in the present or future", violations.iterator().next().getMessage(),
                "The error message should be correct");
        assertEquals("start", violations.iterator().next().getPropertyPath().toString(),
                "The violation should be for the start field");
    }

    @Test
    void bookingDto_WhenEndIsNotInFuture_ShouldFailValidation() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));

        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty(), "There should be validation violations");
        assertEquals(1, violations.size(), "There should be exactly one violation");
        assertEquals("End date must be in the future", violations.iterator().next().getMessage(),
                "The error message should be correct");
        assertEquals("end", violations.iterator().next().getPropertyPath().toString(),
                "The violation should be for the end field");
    }

    @Test
    void bookingDto_WhenStartIsNull_ShouldFailValidation() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(null);
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty(), "There should be validation violations");
        assertEquals(1, violations.size(), "There should be exactly one violation");
        assertEquals("Start date is required", violations.iterator().next().getMessage(),
                "The error message should be correct");
        assertEquals("start", violations.iterator().next().getPropertyPath().toString(),
                "The violation should be for the start field");
    }

    @Test
    void bookingDto_WhenEndIsNull_ShouldFailValidation() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(null);

        Set<ConstraintViolation<BookingCreateDto>> violations = validator.validate(bookingDto);

        assertFalse(violations.isEmpty(), "There should be validation violations");
        assertEquals(1, violations.size(), "There should be exactly one violation");
        assertEquals("End date is required", violations.iterator().next().getMessage(),
                "The error message should be correct");
        assertEquals("end", violations.iterator().next().getPropertyPath().toString(),
                "The violation should be for the end field");
    }
}
