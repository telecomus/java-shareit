package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final Sort SORT_BY_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    @Override
    @Transactional
    public ItemRequestDto create(long userId, ItemRequestDto itemRequestDto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        itemRequest = itemRequestRepository.save(itemRequest);
        log.info("Создан запрос вещи: {}", itemRequest);

        return ItemRequestMapper.toItemRequestDto(itemRequest, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getAllByRequestor(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        List<ItemRequest> requests = itemRequestRepository.findByRequestorId(userId, SORT_BY_CREATED_DESC);
        return getItemRequestDtos(requests);
    }

    @Override
    public List<ItemRequestDto> getAll(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNot(userId, SORT_BY_CREATED_DESC);
        return getItemRequestDtos(requests);
    }

    @Override
    public ItemRequestDto getById(long requestId, long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        List<Item> items = itemRepository.findByRequestId(itemRequest.getId());

        return ItemRequestMapper.toItemRequestDto(itemRequest, items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList()));
    }

    private List<ItemRequestDto> getItemRequestDtos(List<ItemRequest> requests) {
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(
                        requests.stream()
                                .map(ItemRequest::getId)
                                .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> {
                    List<Item> items = itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList());
                    return ItemRequestMapper.toItemRequestDto(request, items.stream()
                            .map(ItemMapper::toItemDto)
                            .collect(Collectors.toList()));
                })
                .collect(Collectors.toList());
    }
}