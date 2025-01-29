package org.crud.bookmarks.service;

import org.crud.bookmarks.Bookmark;
import org.crud.bookmarks.repository.BookmarkRepository;
import org.crud.bookmarks.repository.FolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final FolderRepository folderRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository, FolderRepository folderRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.folderRepository = folderRepository;
    }

    public List<Bookmark> getAllBookmarks() {
        return (List<Bookmark>) bookmarkRepository.findAll();
    }

    public Optional<Bookmark> getBookmarkById(Long id) {
        return bookmarkRepository.findById(id);
    }

    public List<Bookmark> getBookmarksByFolderId(Long folderId) {
        return bookmarkRepository.findByFolderIdOrderByCreatedAtDesc(folderId);
    }

    public Bookmark createBookmark(Bookmark bookmark) {
        if (bookmark.getFolderId() != null) {
            if (!folderRepository.existsById(bookmark.getFolderId())) {
                throw new IllegalArgumentException("Folder not found with id: " + bookmark.getFolderId());
            }
        }
        return bookmarkRepository.save(bookmark);
    }

    public Optional<Bookmark> updateBookmark(Long id, Bookmark bookmarkDetails) {
        return bookmarkRepository.findById(id)
                .map(bookmark -> {
                    bookmark.setTitle(bookmarkDetails.getTitle());
                    bookmark.setUrl(bookmarkDetails.getUrl());
                    bookmark.setDescription(bookmarkDetails.getDescription());
                    if (bookmarkDetails.getFolderId() != null &&
                            !folderRepository.existsById(bookmarkDetails.getFolderId())) {
                        throw new IllegalArgumentException("Folder not found with id: " + bookmarkDetails.getFolderId());
                    }
                    bookmark.setFolderId(bookmarkDetails.getFolderId());
                    return bookmarkRepository.save(bookmark);
                });
    }

    public void deleteBookmark(Long id) {
        bookmarkRepository.deleteById(id);
    }

    public List<Bookmark> searchBookmarks(String searchTerm) {
        return bookmarkRepository.searchBookmarks(searchTerm);
    }

    public int getBookmarkCountInFolder(Long folderId) {
        return bookmarkRepository.countBookmarksInFolder(folderId);
    }
}