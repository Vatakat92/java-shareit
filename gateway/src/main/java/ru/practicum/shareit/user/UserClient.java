package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@Slf4j
@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> create(UserCreateDto userCreateDto) {
        log.debug("Creating user: {}", userCreateDto.getEmail());
        ResponseEntity<Object> response = post("", userCreateDto);
        log.debug("User creation response: status={}, body={}",
                response.getStatusCode(), response.getBody());
        return response;
    }

    public ResponseEntity<Object> getById(Long userId) {
        log.debug("Getting user by id: {}", userId);
        return get("/" + userId, userId);
    }

    public ResponseEntity<Object> getAll() {
        log.debug("Getting all users");
        return get("",null);
    }

    public ResponseEntity<Object> update(Long userId, UserUpdateDto userUpdateDto) {
        log.debug("Updating user: {}, data: {}", userId, userUpdateDto);
        return patch("/" + userId, userId, userUpdateDto);
    }

    public ResponseEntity<Object> delete(Long userId) {
        log.debug("Deleting user: {}", userId);
        return delete("/" + userId, userId);
    }
}