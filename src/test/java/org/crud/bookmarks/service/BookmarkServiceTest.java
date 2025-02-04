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
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BookmarkService}.
 * Tests bookmark CRUD operations and URL validation scenarios.
 */
@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private UrlValidator urlValidator;

    private BookmarkService bookmarkService;

    private Bookmark testBookmark;

    private void mockUrlValidatorSuccess() {
        // Do nothing - validation passes
        doNothing().when(urlValidator).validateUrl(anyString());
    }

    private void mockUrlValidatorError(String errorMessage) {
        doThrow(new InvalidUrlException(errorMessage))
            .when(urlValidator)
            .validateUrl(anyString());
    }

    @BeforeEach
    void setUp() {
        testBookmark = new Bookmark("Test Bookmark", "https://test.com");
        testBookmark.setId(1L);
        testBookmark.setDescription("Test Description");

        bookmarkService = new BookmarkService(bookmarkRepository, folderRepository, urlValidator);
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
        mockUrlValidatorSuccess();
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(testBookmark);

        Bookmark result = bookmarkService.createBookmark(testBookmark);

        assertNotNull(result);
        assertEquals(testBookmark.getTitle(), result.getTitle());
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    void createBookmark_WithInvalidFolderId_ShouldThrowException() {
        testBookmark.setFolderId(999L);
        mockUrlValidatorSuccess();
        when(folderRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
            bookmarkService.createBookmark(testBookmark)
        );
    }

    @Test
    void updateBookmark_WithNullDetails_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> 
            bookmarkService.updateBookmark(1L, null)
        );
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    void updateBookmark_WhenExists_ShouldUpdateBookmark() {
        Bookmark updatedBookmark = new Bookmark("Updated Title", "https://updated.com");
        mockUrlValidatorSuccess();
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
    void createBookmark_WithEmptyUrl_ShouldThrowException() {
        Bookmark emptyUrlBookmark = new Bookmark("Test", "");
        mockUrlValidatorError("URL cannot be null or empty");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            bookmarkService.createBookmark(emptyUrlBookmark)
        );
        assertTrue(exception.getMessage().contains("Invalid bookmark URL"));
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    void createBookmark_WithNullBookmark_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> 
            bookmarkService.createBookmark(null)
        );
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    void createBookmark_WithValidUrl_ShouldCreateBookmark() {
        mockUrlValidatorSuccess();
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(testBookmark);

        Bookmark result = bookmarkService.createBookmark(testBookmark);

        assertNotNull(result);
        assertEquals(testBookmark.getTitle(), result.getTitle());
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    void createBookmark_WithInvalidUrl_ShouldThrowException() {
        String errorMessage = "Resource not accessible (HTTP 404)";
        mockUrlValidatorError(errorMessage);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            bookmarkService.createBookmark(testBookmark)
        );

        assertTrue(exception.getMessage().contains("Invalid bookmark URL"));
        assertTrue(exception.getMessage().contains(errorMessage));
        assertTrue(exception.getCause() instanceof InvalidUrlException);
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    void updateBookmark_WithInvalidUrl_ShouldThrowException() {
        Bookmark updatedBookmark = new Bookmark("Updated Title", "https://invalid.com");
        when(bookmarkRepository.findById(1L)).thenReturn(Optional.of(testBookmark));

        String errorMessage = "Resource not accessible (HTTP 500)";
        mockUrlValidatorError(errorMessage);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            bookmarkService.updateBookmark(1L, updatedBookmark)
        );

        assertTrue(exception.getMessage().contains("Invalid bookmark URL"));
        assertTrue(exception.getMessage().contains(errorMessage));
        assertTrue(exception.getCause() instanceof InvalidUrlException);
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    void createBookmark_WithTimeout_ShouldThrowException() {
        String errorMessage = "Connection timeout";
        mockUrlValidatorError(errorMessage);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            bookmarkService.createBookmark(testBookmark)
        );

        assertTrue(exception.getMessage().contains("Invalid bookmark URL"));
        assertTrue(exception.getMessage().contains(errorMessage));
        assertTrue(exception.getCause() instanceof InvalidUrlException);
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    void searchBookmarks_ShouldReturnMatchingBookmarks() {
        List<Bookmark> bookmarks = Arrays.asList(testBookmark);
        when(bookmarkRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("Test", "Test")).thenReturn(bookmarks);

        List<Bookmark> result = bookmarkService.searchBookmarks("Test");

        assertEquals(bookmarks.size(), result.size());
        assertEquals(bookmarks.get(0).getTitle(), result.get(0).getTitle());
    }
}
