package ru.practicum.shareit.request.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.validation.Create;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                        @Validated(Create.class) @RequestBody ItemRequestDto requestDto) {
        log.info("Request API create: userId={} description={}", userId, requestDto.getDescription());
        try {
            ItemRequestDto created = requestService.createRequest(userId, requestDto);
            URI location = URI.create("/requests/" + created.getId());
            log.info("Request API created: id={} userId={}", created.getId(), userId);
            return ResponseEntity.created(location).body(created);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            log.warn("User not found: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Validation error when creating request: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Request API list user requests: userId={}", userId);
        List<ItemRequestDto> requests = requestService.getUserRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                               @RequestParam(defaultValue = "0") @Min(0) int from,
                                                               @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.debug("Request API list all requests: userId={} from={} size={}", userId, from, size);
        List<ItemRequestDto> requests = requestService.getAllRequests(userId, from, size);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable Long requestId) {
        log.debug("Request API get by id: userId={} requestId={}", userId, requestId);
        ItemRequestDto request = requestService.getRequest(userId, requestId);
        return ResponseEntity.ok(request);
    }
}