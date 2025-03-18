package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final String itemsIdPath = "/{id}";
    private final String searchPath = "/search";
    private final String commentPath = "/{itemId}/comment";
    private final String userIdHeader = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @GetMapping()
    public ResponseEntity<Object> findAllFromUser(
            @RequestHeader(value = userIdHeader, required = false) Long userId) {
        return itemClient.findAllFromUser(userId);
    }

    @GetMapping(itemsIdPath)
    public ResponseEntity<Object> findItem(@PathVariable Long id,
                                        @RequestHeader(value = userIdHeader, required = false) Long userId) {
        return itemClient.findById(id, userId);
    }

    @GetMapping(searchPath)
    public ResponseEntity<Object> findItemByText(@RequestParam(required = false) String text,
                                                 @RequestHeader(value = userIdHeader, required = false) Long userId) {
        return itemClient.findByText(text, userId);
    }

    @PostMapping()
    public ResponseEntity<Object> create(@Valid @RequestBody ItemCreateDto itemDto,
                          @RequestHeader(value = userIdHeader, required = false) Long userId) {
        return itemClient.create(itemDto, userId);
    }

    @PatchMapping(itemsIdPath)
    public ResponseEntity<Object> update(@RequestBody ItemUpdateDto itemDto,
                          @RequestHeader(value = userIdHeader, required = false) Long userId,
                          @PathVariable Long id) {
        return itemClient.update(itemDto, userId, id);
    }

    @PostMapping(commentPath)
    public ResponseEntity<Object> createComment(@Valid @RequestBody CommentCreateDto comment,
                                    @RequestHeader(value = userIdHeader, required = false) Long userId,
                                    @PathVariable Long itemId) {
        return itemClient.createComment(comment, userId, itemId);
    }
}

