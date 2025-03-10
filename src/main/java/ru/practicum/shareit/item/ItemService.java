package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

import java.util.List;

public interface ItemService {
    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, long itemId, ItemDto itemDto);

    ItemWithBookingDto getById(long itemId, long userId);

    List<ItemWithBookingDto> getAllByUserId(long userId);

    List<ItemDto> search(String text);

    CommentDto createComment(long userId, long itemId, CommentRequestDto commentDto);
}