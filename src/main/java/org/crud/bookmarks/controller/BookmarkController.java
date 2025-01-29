package org.crud.bookmarks.controller;

import org.crud.bookmarks.Bookmark;
import org.crud.bookmarks.service.BookmarkService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@CrossOrigin(origins = "*")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    public List<Bookmark> getAllBookmarks() {
        System.out.println("[DEBUG_LOG] Getting all bookmarks");
        List<Bookmark> bookmarks = bookmarkService.getAllBookmarks();
        System.out.println("[DEBUG_LOG] Retrieved " + bookmarks.size() + " bookmarks");
        return bookmarks;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bookmark> getBookmarkById(@PathVariable Long id) {
        return bookmarkService.getBookmarkById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/folder/{folderId}")
    public Page<Bookmark> getBookmarksByFolderId(
            @PathVariable Long folderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return bookmarkService.getBookmarksByFolderId(folderId, pageRequest);
    }

    @PostMapping
    public ResponseEntity<Bookmark> createBookmark(@Valid @RequestBody Bookmark bookmark) {
        System.out.println("[DEBUG_LOG] Creating bookmark: " + bookmark);
        Bookmark createdBookmark = bookmarkService.createBookmark(bookmark);
        System.out.println("[DEBUG_LOG] Created bookmark: " + createdBookmark);
        return ResponseEntity.ok(createdBookmark);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bookmark> updateBookmark(@PathVariable Long id, @Valid @RequestBody Bookmark bookmarkDetails) {
        return bookmarkService.updateBookmark(id, bookmarkDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long id) {
        bookmarkService.deleteBookmark(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> deleteBookmarks(@RequestBody List<Long> ids) {
        bookmarkService.deleteBookmarks(ids);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public Page<Bookmark> searchBookmarks(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return bookmarkService.searchBookmarks(query, pageRequest);
    }

    @GetMapping("/folder/{folderId}/count")
    public ResponseEntity<Integer> getBookmarkCountInFolder(@PathVariable Long folderId) {
        int count = bookmarkService.getBookmarkCountInFolder(folderId);
        return ResponseEntity.ok(count);
    }
}
