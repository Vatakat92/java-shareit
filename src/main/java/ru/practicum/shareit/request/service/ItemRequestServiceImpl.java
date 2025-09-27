package ru.practicum.shareit.request.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        requestDto.setCreated(LocalDateTime.now());
        ItemRequest request = ItemRequestMapper.toEntity(requestDto, user);
        return ItemRequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        return requestRepository.findByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(request -> {
                    ItemRequestDto dto = ItemRequestMapper.toDto(request);
                    List<ItemDto> items = itemRepository.findAll().stream()
                            .filter(item -> item.getRequest() != null && item.getRequest().getId().equals(request.getId()))
                            .map(ItemMapper::toDto)
                            .collect(Collectors.toList());
                    dto.setItems(items);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        PageRequest page = PageRequest.of(from / size, size);
        return requestRepository.findByRequestorIdNot(userId, page).stream()
                .map(request -> {
                    ItemRequestDto dto = ItemRequestMapper.toDto(request);
                    List<ItemDto> items = itemRepository.findAll().stream()
                            .filter(item -> item.getRequest() != null && item.getRequest().getId().equals(request.getId()))
                            .map(ItemMapper::toDto)
                            .collect(Collectors.toList());
                    dto.setItems(items);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequest(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + requestId));
        ItemRequestDto dto = ItemRequestMapper.toDto(request);
        List<ItemDto> items = itemRepository.findAll().stream()
                .filter(item -> item.getRequest() != null && item.getRequest().getId().equals(requestId))
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
        dto.setItems(items);
        return dto;
    }
}