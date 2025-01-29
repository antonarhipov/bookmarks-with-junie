package org.crud.bookmarks.repository;

import org.crud.bookmarks.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends CrudRepository<Bookmark, Long>, PagingAndSortingRepository<Bookmark, Long> {

    List<Bookmark> findByFolderId(Long folderId);

    Page<Bookmark> findByFolderId(Long folderId, Pageable pageable);

    List<Bookmark> findByFolderIdOrderByCreatedAtDesc(Long folderId);

    List<Bookmark> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    Page<Bookmark> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);

    int countByFolderId(Long folderId);

    default List<Bookmark> searchBookmarks(String searchTerm) {
        return findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchTerm, searchTerm);
    }
}
