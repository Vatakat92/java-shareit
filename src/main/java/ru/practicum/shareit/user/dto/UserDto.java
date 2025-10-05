package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.shareit.validation.Create;
import ru.practicum.shareit.validation.Update;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserDto {
    private Long id;

    @NotBlank(message = "Name cannot be blank", groups = {Create.class})
    private String name;

    @NotBlank(message = "Email cannot be blank", groups = {Create.class})
    @Email(message = "Email must be valid", groups = {Create.class, Update.class})
    private String email;
}