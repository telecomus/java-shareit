package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommentMapperTest {

    @Test
    void toCommentDto_WithValidComment_ShouldReturnCommentDto() {
        // Arrange
        User author = new User(2L, "Author", "author@example.com");
        Item item = new Item(1L, "Drill", "Electric drill", true, new User(), null);
        LocalDateTime created = LocalDateTime.now();

        Comment comment = new Comment(1L, "Great drill!", item, author, created);

        // Act
        CommentDto commentDto = CommentMapper.toCommentDto(comment);

        // Assert
        assertEquals(1L, commentDto.getId());
        assertEquals("Great drill!", commentDto.getText());
        assertEquals("Author", commentDto.getAuthorName());
        assertEquals(created, commentDto.getCreated());
    }

    @Test
    void toCommentDto_WithNullComment_ShouldHandleNullSafely() {
        // Act
        CommentDto commentDto = CommentMapper.toCommentDto(null);

        // Assert
        assertNull(commentDto);
    }

    @Test
    void toComment_WithValidCommentRequestDto_ShouldReturnComment() {
        // Arrange
        User author = new User(2L, "Author", "author@example.com");
        Item item = new Item(1L, "Drill", "Electric drill", true, new User(), null);
        LocalDateTime created = LocalDateTime.now();

        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Great drill!");

        // Act
        Comment comment = CommentMapper.toComment(commentRequestDto, item, author, created);

        // Assert
        assertEquals("Great drill!", comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(author, comment.getAuthor());
        assertEquals(created, comment.getCreated());
    }

    @Test
    void toComment_WithNullCommentRequestDto_ShouldHandleNullSafely() {
        // Arrange
        User author = new User(2L, "Author", "author@example.com");
        Item item = new Item(1L, "Drill", "Electric drill", true, new User(), null);
        LocalDateTime created = LocalDateTime.now();

        // Act
        Comment comment = CommentMapper.toComment(null, item, author, created);

        // Assert
        assertNull(comment);
    }
}