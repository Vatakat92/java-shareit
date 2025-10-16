package ru.practicum.shareit.request.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestInternalDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestResponseDto createRequest(Long userId, ItemRequestInternalDto requestDto) {
        if (requestDto == null) {
            throw new IllegalArgumentException("Request DTO cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        ItemRequest request = ItemRequestMapper.toEntity(requestDto, user);
        return ItemRequestMapper.toResponseDto(requestRepository.save(request));
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        return mapRequestsToResponseDtoWithItems(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        PageRequest page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> requests = requestRepository.findByRequestorIdNot(userId, page).getContent();
        return mapRequestsToResponseDtoWithItems(requests);
    }

    @Override
    public ItemRequestResponseDto getRequest(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + requestId));
        return mapRequestsToResponseDtoWithItems(List.of(request)).getFirst();
    }

    private List<ItemRequestResponseDto> mapRequestsToResponseDtoWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(toList());
        List<Item> items = itemRepository.findByRequestIdIn(requestIds);

        Map<Long, List<ItemShortDto>> itemsByRequestId = items.stream()
                .map(item -> ItemShortDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                        .build())
                .collect(groupingBy(ItemShortDto::getRequestId, toList()));

        return requests.stream()
                .map(request -> {
                    ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request);
                    List<ItemShortDto> requestItems = itemsByRequestId.getOrDefault(request.getId(), List.of());
                    dto.setItems(requestItems);
                    return dto;
                })
                .collect(toList());
    }
}