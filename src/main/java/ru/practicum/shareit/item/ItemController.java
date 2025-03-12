package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.util.Constants;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(Constants.USER_ID_HEADER) long userId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на создание вещи пользователем с id {}", userId);
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(Constants.USER_ID_HEADER) long userId,
                          @PathVariable long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на обновление вещи с id {} пользователем с id {}", itemId, userId);
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingDto getById(@PathVariable long itemId,
                                      @RequestHeader(Constants.USER_ID_HEADER) long userId) {
        log.info("Получен запрос на получение вещи с id {}", itemId);
        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemWithBookingDto> getAllByUserId(@RequestHeader(Constants.USER_ID_HEADER) long userId) {
        log.info("Получен запрос на получение всех вещей пользователя с id {}", userId);
        return itemService.getAllByUserId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Получен запрос на поиск вещей по тексту: {}", text);
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader(Constants.USER_ID_HEADER) long userId,
                                    @PathVariable long itemId,
                                    @RequestBody CommentRequestDto commentDto) {
        log.info("Получен запрос на создание комментария к вещи с id {} от пользователя с id {}", itemId, userId);
        return itemService.createComment(userId, itemId, commentDto);
    }
}