package org.crud.bookmarks.service;

import org.crud.bookmarks.Bookmark;
import org.crud.bookmarks.repository.BookmarkRepository;
import org.crud.bookmarks.repository.FolderRepository;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookmarkService {

    private static final Logger logger = LoggerFactory.getLogger(BookmarkService.class);

    private final BookmarkRepository bookmarkRepository;
    private final FolderRepository folderRepository;
    private final UrlValidator urlValidator;

    public BookmarkService(BookmarkRepository bookmarkRepository, FolderRepository folderRepository, UrlValidator urlValidator) {
        this.bookmarkRepository = bookmarkRepository;
        this.folderRepository = folderRepository;
        this.urlValidator = urlValidator;
    }

    public List<Bookmark> getAllBookmarks() {
        logger.debug("Fetching all bookmarks");
        List<Bookmark> bookmarks = (List<Bookmark>) bookmarkRepository.findAll();
        logger.debug("Fetched {} bookmarks", bookmarks.size());
        return bookmarks;
    }

    public Page<Bookmark> getAllBookmarks(Pageable pageable) {
        logger.debug("Fetching all bookmarks with pagination: {}", pageable);
        Page<Bookmark> bookmarks = bookmarkRepository.findAll(pageable);
        logger.debug("Fetched {} bookmarks", bookmarks.getTotalElements());
        return bookmarks;
    }

    public Optional<Bookmark> getBookmarkById(Long id) {
        logger.debug("Fetching bookmark with id: {}", id);
        Optional<Bookmark> bookmark = bookmarkRepository.findById(id);
        logger.debug("Fetch result for id {}: {}", id, bookmark.isPresent() ? "found" : "not found");
        return bookmark;
    }

    public Page<Bookmark> getBookmarksByFolderId(Long folderId, Pageable pageable) {
        logger.debug("Fetching bookmarks for folderId: {}", folderId);
        Page<Bookmark> bookmarks = bookmarkRepository.findByFolderId(folderId, pageable);
        logger.debug("Fetched {} bookmarks for folderId: {}", bookmarks.getTotalElements(), folderId);
        return bookmarks;
    }

    @Transactional
    public void deleteBookmarks(List<Long> ids) {
        logger.debug("Deleting bookmarks with ids: {}", ids);
        bookmarkRepository.deleteAllById(ids);
        logger.debug("Deleted {} bookmarks", ids.size());
    }

    /**
     * Validates the URL using the URL validator.
     * Wraps any validation errors in a descriptive IllegalArgumentException.
     *
     * @param url the URL to validate
     * @throws IllegalArgumentException if the URL is invalid or inaccessible
     */
    private void validateUrl(String url) {
        try {
            urlValidator.validateUrl(url);
        } catch (InvalidUrlException e) {
            throw new IllegalArgumentException("Invalid bookmark URL: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new bookmark with the given details.
     * Validates the URL and folder (if specified) before saving.
     *
     * @param bookmark the bookmark to create
     * @return the created bookmark
     * @throws IllegalArgumentException if the URL is invalid or the specified folder doesn't exist
     */
    @Transactional
    public Bookmark createBookmark(Bookmark bookmark) {
        logger.debug("Creating new bookmark: {}", bookmark);

        if (bookmark == null) {
            throw new IllegalArgumentException("Bookmark cannot be null");
        }

        validateUrl(bookmark.getUrl());

        if (bookmark.getFolderId() != null) {
            logger.debug("Checking existence of folder with id: {}", bookmark.getFolderId());
            if (!folderRepository.existsById(bookmark.getFolderId())) {
                logger.error("Folder not found with id: {}", bookmark.getFolderId());
                throw new IllegalArgumentException("Cannot create bookmark: Folder not found with id " + bookmark.getFolderId());
            }
        }

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        logger.debug("Created bookmark: {}", savedBookmark);
        return savedBookmark;
    }

    /**
     * Updates an existing bookmark with new details.
     * Validates the URL and folder (if specified) before updating.
     *
     * @param id the ID of the bookmark to update
     * @param bookmarkDetails the new bookmark details
     * @return Optional containing the updated bookmark, or empty if the bookmark wasn't found
     * @throws IllegalArgumentException if the URL is invalid or the specified folder doesn't exist
     */
    @Transactional
    public Optional<Bookmark> updateBookmark(Long id, Bookmark bookmarkDetails) {
        logger.debug("Updating bookmark with id: {} with details: {}", id, bookmarkDetails);

        if (bookmarkDetails == null) {
            throw new IllegalArgumentException("Bookmark details cannot be null");
        }

        return bookmarkRepository.findById(id)
                .map(bookmark -> {
                    bookmark.setTitle(bookmarkDetails.getTitle());
                    validateUrl(bookmarkDetails.getUrl());
                    bookmark.setUrl(bookmarkDetails.getUrl());
                    bookmark.setDescription(bookmarkDetails.getDescription());

                    if (bookmarkDetails.getFolderId() != null) {
                        logger.debug("Checking existence of folder with id: {}", bookmarkDetails.getFolderId());
                        if (!folderRepository.existsById(bookmarkDetails.getFolderId())) {
                            logger.error("Folder not found with id: {}", bookmarkDetails.getFolderId());
                            throw new IllegalArgumentException("Cannot update bookmark: Folder not found with id " + bookmarkDetails.getFolderId());
                        }
                    }

                    bookmark.setFolderId(bookmarkDetails.getFolderId());
                    Bookmark updatedBookmark = bookmarkRepository.save(bookmark);
                    logger.debug("Updated bookmark: {}", updatedBookmark);
                    return updatedBookmark;
                });
    }

    public void deleteBookmark(Long id) {
        logger.debug("Deleting bookmark with id: {}", id);
        bookmarkRepository.deleteById(id);
        logger.debug("Deleted bookmark with id: {}", id);
    }

    public List<Bookmark> searchBookmarks(String searchTerm) {
        logger.debug("Searching for bookmarks with term: {}", searchTerm);
        List<Bookmark> bookmarks = bookmarkRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm, searchTerm);
        logger.debug("Found {} bookmarks for searchTerm: {}", bookmarks.size(), searchTerm);
        return bookmarks;
    }

    public Page<Bookmark> searchBookmarks(String searchTerm, Pageable pageable) {
        logger.debug("Searching for bookmarks with term: {} and pageable: {}", searchTerm, pageable);
        Page<Bookmark> bookmarks = bookmarkRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm, searchTerm, pageable);
        logger.debug("Found {} bookmarks for searchTerm: {}", bookmarks.getTotalElements(), searchTerm);
        return bookmarks;
    }

    public int getBookmarkCountInFolder(Long folderId) {
        logger.debug("Counting bookmarks in folder with id: {}", folderId);
        int count = bookmarkRepository.countByFolderId(folderId);
        logger.debug("Found {} bookmarks in folder with id: {}", count, folderId);
        return count;
    }
}
