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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookmarkService {

    private static final Logger logger = LoggerFactory.getLogger(BookmarkService.class);

    private final BookmarkRepository bookmarkRepository;
    private final FolderRepository folderRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository, FolderRepository folderRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.folderRepository = folderRepository;
    }

    public List<Bookmark> getAllBookmarks() {
        logger.debug("Fetching all bookmarks");
        List<Bookmark> bookmarks = (List<Bookmark>) bookmarkRepository.findAll();
        logger.debug("Fetched {} bookmarks", bookmarks.size());
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

    @Transactional
    public Bookmark createBookmark(Bookmark bookmark) {
        logger.debug("Creating new bookmark: {}", bookmark);
        if (bookmark.getFolderId() != null) {
            logger.debug("Checking existence of folder with id: {}", bookmark.getFolderId());
            if (!folderRepository.existsById(bookmark.getFolderId())) {
                logger.error("Folder not found with id: {}", bookmark.getFolderId());
                throw new IllegalArgumentException("Folder not found with id: " + bookmark.getFolderId());
            }
        }
        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        logger.debug("Created bookmark: {}", savedBookmark);
        return savedBookmark;
    }

    @Transactional
    public Optional<Bookmark> updateBookmark(Long id, Bookmark bookmarkDetails) {
        logger.debug("Updating bookmark with id: {} with details: {}", id, bookmarkDetails);
        return bookmarkRepository.findById(id)
                .map(bookmark -> {
                    bookmark.setTitle(bookmarkDetails.getTitle());
                    bookmark.setUrl(bookmarkDetails.getUrl());
                    bookmark.setDescription(bookmarkDetails.getDescription());
                    if (bookmarkDetails.getFolderId() != null) {
                        logger.debug("Checking existence of folder with id: {}", bookmarkDetails.getFolderId());
                        if (!folderRepository.existsById(bookmarkDetails.getFolderId())) {
                            logger.error("Folder not found with id: {}", bookmarkDetails.getFolderId());
                            throw new IllegalArgumentException("Folder not found with id: " + bookmarkDetails.getFolderId());
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
