package org.crud.bookmarks.controller;

import org.crud.bookmarks.Bookmark;
import org.crud.bookmarks.service.BookmarkService;
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
        return bookmarkService.getAllBookmarks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bookmark> getBookmarkById(@PathVariable Long id) {
        return bookmarkService.getBookmarkById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/folder/{folderId}")
    public List<Bookmark> getBookmarksByFolderId(@PathVariable Long folderId) {
        return bookmarkService.getBookmarksByFolderId(folderId);
    }

    @PostMapping
    public ResponseEntity<Bookmark> createBookmark(@Valid @RequestBody Bookmark bookmark) {
        Bookmark createdBookmark = bookmarkService.createBookmark(bookmark);
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

    @GetMapping("/search")
    public List<Bookmark> searchBookmarks(@RequestParam String query) {
        return bookmarkService.searchBookmarks(query);
    }

    @GetMapping("/folder/{folderId}/count")
    public ResponseEntity<Integer> getBookmarkCountInFolder(@PathVariable Long folderId) {
        int count = bookmarkService.getBookmarkCountInFolder(folderId);
        return ResponseEntity.ok(count);
    }
}