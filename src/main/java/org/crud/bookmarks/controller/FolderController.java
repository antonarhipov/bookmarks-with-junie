package org.crud.bookmarks.controller;

import org.crud.bookmarks.Folder;
import org.crud.bookmarks.service.FolderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@CrossOrigin(origins = "*")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    public List<Folder> getAllFolders() {
        return folderService.getAllFolders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Folder> getFolderById(@PathVariable Long id) {
        return folderService.getFolderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Folder> createFolder(@Valid @RequestBody Folder folder) {
        try {
            Folder createdFolder = folderService.createFolder(folder);
            return ResponseEntity.ok(createdFolder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Folder> updateFolder(@PathVariable Long id, @Valid @RequestBody Folder folderDetails) {
        try {
            return folderService.updateFolder(id, folderDetails)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long id) {
        try {
            folderService.deleteFolder(id);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public List<Folder> searchFolders(@RequestParam String query) {
        return folderService.searchFolders(query);
    }

    @GetMapping("/with-count")
    public List<Folder> getAllFoldersWithBookmarkCount() {
        return folderService.getAllFoldersWithBookmarkCount();
    }
}