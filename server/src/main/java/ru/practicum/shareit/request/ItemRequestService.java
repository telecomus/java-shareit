package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getAllByRequestor(long userId);

    List<ItemRequestDto> getAll(long userId);

    ItemRequestDto getById(long requestId, long userId);
}