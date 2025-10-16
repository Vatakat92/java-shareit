package ru.practicum.shareit.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("unused")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldErrorDto {

    private String field;

    private String message;
}