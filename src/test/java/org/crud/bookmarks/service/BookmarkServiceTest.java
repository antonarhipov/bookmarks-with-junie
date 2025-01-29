package org.crud.bookmarks.service;

import org.crud.bookmarks.Bookmark;
import org.crud.bookmarks.repository.BookmarkRepository;
import org.crud.bookmarks.repository.FolderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private FolderRepository folderRepository;

    @InjectMocks
    private BookmarkService bookmarkService;

    private Bookmark testBookmark;

    @BeforeEach
    void setUp() {
        testBookmark = new Bookmark("Test Bookmark", "https://test.com");
        testBookmark.setId(1L);
        testBookmark.setDescription("Test Description");
    }

    @Test
    void getAllBookmarks_ShouldReturnAllBookmarks() {
        List<Bookmark> bookmarks = Arrays.asList(testBookmark);
        when(bookmarkRepository.findAll()).thenReturn(bookmarks);

        List<Bookmark> result = bookmarkService.getAllBookmarks();

        assertEquals(bookmarks.size(), result.size());
        assertEquals(bookmarks.get(0).getTitle(), result.get(0).getTitle());
    }

    @Test
    void getBookmarkById_WhenExists_ShouldReturnBookmark() {
        when(bookmarkRepository.findById(1L)).thenReturn(Optional.of(testBookmark));

        Optional<Bookmark> result = bookmarkService.getBookmarkById(1L);

        assertTrue(result.isPresent());
        assertEquals(testBookmark.getTitle(), result.get().getTitle());
    }

    @Test
    void createBookmark_WithValidData_ShouldCreateBookmark() {
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(testBookmark);

        Bookmark result = bookmarkService.createBookmark(testBookmark);

        assertNotNull(result);
        assertEquals(testBookmark.getTitle(), result.getTitle());
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    void createBookmark_WithInvalidFolderId_ShouldThrowException() {
        testBookmark.setFolderId(999L);
        when(folderRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
            bookmarkService.createBookmark(testBookmark)
        );
    }

    @Test
    void updateBookmark_WhenExists_ShouldUpdateBookmark() {
        Bookmark updatedBookmark = new Bookmark("Updated Title", "https://updated.com");
        when(bookmarkRepository.findById(1L)).thenReturn(Optional.of(testBookmark));
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(updatedBookmark);

        Optional<Bookmark> result = bookmarkService.updateBookmark(1L, updatedBookmark);

        assertTrue(result.isPresent());
        assertEquals(updatedBookmark.getTitle(), result.get().getTitle());
    }

    @Test
    void deleteBookmark_ShouldDeleteBookmark() {
        bookmarkService.deleteBookmark(1L);
        verify(bookmarkRepository).deleteById(1L);
    }

    @Test
    void searchBookmarks_ShouldReturnMatchingBookmarks() {
        List<Bookmark> bookmarks = Arrays.asList(testBookmark);
        when(bookmarkRepository.searchBookmarks("Test")).thenReturn(bookmarks);

        List<Bookmark> result = bookmarkService.searchBookmarks("Test");

        assertEquals(bookmarks.size(), result.size());
        assertEquals(bookmarks.get(0).getTitle(), result.get(0).getTitle());
    }
}