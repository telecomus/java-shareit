package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(long userId, ItemDto itemDto) {
        validateNewItem(itemDto);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        item = itemRepository.save(item);
        log.info("Создана вещь: {}", item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

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
        item = itemRepository.save(item);
        log.info("Обновлена вещь: {}", item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemWithBookingDto getById(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;

        // Добавляем информацию о бронированиях только для владельца вещи
        if (item.getOwner().getId() == userId) {
            LocalDateTime now = LocalDateTime.now();

            // Последнее бронирование
            List<Booking> lastBookings = bookingRepository.findLastBookingForItem(itemId, now);
            if (!lastBookings.isEmpty()) {
                lastBooking = BookingMapper.toBookingShortDto(lastBookings.get(0));
            }

            // Следующее бронирование
            List<Booking> nextBookings = bookingRepository.findNextBookingForItem(itemId, now);
            if (!nextBookings.isEmpty()) {
                nextBooking = BookingMapper.toBookingShortDto(nextBookings.get(0));
            }
        }

        // Получаем комментарии к вещи
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        return ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemWithBookingDto> getAllByUserId(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        List<Item> items = itemRepository.findByOwnerId(userId);

        // Получаем id всех вещей пользователя
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        // Получаем все комментарии для вещей пользователя
        Map<Long, List<CommentDto>> commentsMap = commentRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    // Последнее бронирование
                    List<Booking> lastBookings = bookingRepository.findLastBookingForItem(item.getId(), now);
                    BookingShortDto lastBooking = lastBookings.isEmpty() ? null :
                            BookingMapper.toBookingShortDto(lastBookings.get(0));

                    // Следующее бронирование
                    List<Booking> nextBookings = bookingRepository.findNextBookingForItem(item.getId(), now);
                    BookingShortDto nextBooking = nextBookings.isEmpty() ? null :
                            BookingMapper.toBookingShortDto(nextBookings.get(0));

                    // Комментарии для данной вещи
                    List<CommentDto> comments = commentsMap.getOrDefault(item.getId(), Collections.emptyList());

                    return ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(long userId, long itemId, CommentRequestDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        LocalDateTime now = LocalDateTime.now();

        // Проверяем, что пользователь брал вещь в аренду и аренда уже завершилась
        boolean hasBookedItem = bookingRepository.hasUserBookedItem(itemId, userId, now);
        if (!hasBookedItem) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду или аренда ещё не завершена");
        }

        // Проверяем текст комментария
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Comment comment = CommentMapper.toComment(commentDto, item, author, now);
        comment = commentRepository.save(comment);
        log.info("Создан комментарий: {}", comment);

        return CommentMapper.toCommentDto(comment);
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