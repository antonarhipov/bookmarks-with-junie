package org.crud.bookmarks;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing a folder that contains bookmarks.
 * Folders help organize bookmarks into logical groups.
 */
@Table("FOLDERS")
public class Folder {

    @Id
    private Long id;

    @NotBlank(message = "Folder name is required")
    @Size(max = 255, message = "Folder name must not exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Transient
    private List<Bookmark> bookmarks = new ArrayList<>();

    // Default constructor
    public Folder() {
    }

    // Constructor with required fields
    public Folder(String name) {
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<Bookmark> bookmarks) {
        this.bookmarks = bookmarks != null ? bookmarks : new ArrayList<>();
    }

    public void addBookmark(Bookmark bookmark) {
        if (bookmarks == null) {
            bookmarks = new ArrayList<>();
        }
        bookmarks.add(bookmark);
    }

    public void removeBookmark(Bookmark bookmark) {
        if (bookmarks != null) {
            bookmarks.remove(bookmark);
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
