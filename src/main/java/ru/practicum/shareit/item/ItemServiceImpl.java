package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final Map<Long, Item> items = new HashMap<>();
    private final UserService userService;
    private Long nextId = 1L;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        validateNewItem(itemDto);
        userService.getById(userId); // проверяем существование пользователя

        Item item = ItemMapper.toItem(itemDto);
        item.setId(nextId++);

        User owner = new User();
        owner.setId(userId);
        item.setOwner(owner);

        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }

        if (item.getOwner().getId() != userId) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getById(long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByUserId(long userId) {
        userService.getById(userId); // проверяем существование пользователя
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == userId)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(searchText) ||
                        item.getDescription().toLowerCase().contains(searchText))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void validateNewItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности вещи должен быть указан");
        }
    }
}