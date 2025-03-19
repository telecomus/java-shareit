package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.util.Constants;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final String requestsIdPath = "/{requestId}";
    private final String allPath = "/all";
    private final ItemRequestClient itemRequestClient;

    @Autowired
    public ItemRequestController(ItemRequestClient itemRequestClient) {
        this.itemRequestClient = itemRequestClient;
    }

    @GetMapping(requestsIdPath)
    public ResponseEntity<Object> findById(@PathVariable Long requestId,
                                           @RequestHeader(value = Constants.USER_ID_HEADER, required = false) Long userId) {
        return itemRequestClient.findById(requestId, userId);
    }

    @GetMapping(allPath)
    public ResponseEntity<Object> findAll(@RequestHeader(value = Constants.USER_ID_HEADER, required = false) Long userId) {
        return itemRequestClient.findAll(userId);
    }

    @GetMapping()
    public ResponseEntity<Object> findByRequestorId(@RequestHeader(value = Constants.USER_ID_HEADER, required = false)
                                                    Long requestorId) {
        return itemRequestClient.findByRequestorId(requestorId);
    }

    @PostMapping()
    public ResponseEntity<Object> createItemRequest(@Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto,
                                                    @RequestHeader(value = Constants.USER_ID_HEADER, required = false) Long requestorId) {
        return itemRequestClient.createItemRequest(itemRequestCreateDto, requestorId);
    }
}