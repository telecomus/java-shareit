package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.util.Constants;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final String itemsIdPath = "/{id}";
    private final String searchPath = "/search";
    private final String commentPath = "/{itemId}/comment";
    private final ItemClient itemClient;

    @Autowired
    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @GetMapping()
    public ResponseEntity<Object> findAllFromUser(
            @RequestHeader(value = Constants.USER_ID_HEADER, required = false) Long userId) {
        return itemClient.findAllFromUser(userId);
    }

    @GetMapping(itemsIdPath)
    public ResponseEntity<Object> findItem(@PathVariable Long id,
                                           @RequestHeader(value = Constants.USER_ID_HEADER, required = false) Long userId) {
        return itemClient.findById(id, userId);
    }

    @GetMapping(searchPath)
    public ResponseEntity<Object> findItemByText(@RequestParam(required = false) String text,
                                                 @RequestHeader(value = Constants.USER_ID_HEADER, required = false) Long userId) {
        return itemClient.findByText(text, userId);
    }

    @PostMapping()
    public ResponseEntity<Object> create(@Valid @RequestBody ItemCreateDto itemDto,
                                         @RequestHeader(value = Constants.USER_ID_HEADER, required = false) Long userId) {
        return itemClient.create(itemDto, userId);
    }

    @PatchMapping(itemsIdPath)
    public ResponseEntity<Object> update(@RequestBody ItemUpdateDto itemDto,
                                         @RequestHeader(value = Constants.USER_ID_HEADER, required = false) Long userId,
                                         @PathVariable Long id) {
        return itemClient.update(itemDto, userId, id);
    }

    @PostMapping(commentPath)
    public ResponseEntity<Object> createComment(@Valid @RequestBody CommentCreateDto comment,
                                                @RequestHeader(value = Constants.USER_ID_HEADER, required = false) Long userId,
                                                @PathVariable Long itemId) {
        return itemClient.createComment(comment, userId, itemId);
    }
}