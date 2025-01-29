package org.crud.bookmarks.repository;

import org.crud.bookmarks.Bookmark;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends CrudRepository<Bookmark, Long> {
    
    List<Bookmark> findByFolderId(Long folderId);
    
    @Query("SELECT * FROM bookmarks WHERE folder_id = :folderId ORDER BY created_at DESC")
    List<Bookmark> findByFolderIdOrderByCreatedAtDesc(@Param("folderId") Long folderId);
    
    @Query("SELECT * FROM bookmarks WHERE LOWER(title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Bookmark> searchBookmarks(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(*) FROM bookmarks WHERE folder_id = :folderId")
    int countBookmarksInFolder(@Param("folderId") Long folderId);
}