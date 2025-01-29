package org.crud.bookmarks.repository;

import org.crud.bookmarks.Folder;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends CrudRepository<Folder, Long> {
    
    @Query("SELECT * FROM folders ORDER BY name ASC")
    List<Folder> findAllOrderByName();
    
    @Query("SELECT * FROM folders WHERE LOWER(name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Folder> searchFolders(@Param("searchTerm") String searchTerm);
    
    boolean existsByName(String name);
    
    @Query("SELECT f.*, COUNT(b.id) as bookmark_count " +
           "FROM folders f " +
           "LEFT JOIN bookmarks b ON f.id = b.folder_id " +
           "GROUP BY f.id")
    List<Folder> findAllWithBookmarkCount();
}