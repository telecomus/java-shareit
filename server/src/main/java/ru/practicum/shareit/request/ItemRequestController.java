package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.util.Constants;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(Constants.USER_ID_HEADER) long userId,
                                 @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Получен запрос на создание запроса вещи пользователем с id {}", userId);
        return itemRequestService.create(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllByRequestor(@RequestHeader(Constants.USER_ID_HEADER) long userId) {
        log.info("Получен запрос на получение всех запросов пользователя с id {}", userId);
        return itemRequestService.getAllByRequestor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(Constants.USER_ID_HEADER) long userId) {
        log.info("Получен запрос на получение всех запросов от пользователя с id {}", userId);
        return itemRequestService.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@PathVariable long requestId,
                                  @RequestHeader(Constants.USER_ID_HEADER) long userId) {
        log.info("Получен запрос на получение запроса с id {} от пользователя с id {}", requestId, userId);
        return itemRequestService.getById(requestId, userId);
    }
}