package org.crud.bookmarks.service;

import org.crud.bookmarks.Folder;
import org.crud.bookmarks.repository.BookmarkRepository;
import org.crud.bookmarks.repository.FolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FolderService {

    private final FolderRepository folderRepository;
    private final BookmarkRepository bookmarkRepository;

    public FolderService(FolderRepository folderRepository, BookmarkRepository bookmarkRepository) {
        this.folderRepository = folderRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    public List<Folder> getAllFolders() {
        return folderRepository.findAllOrderByName();
    }

    public Optional<Folder> getFolderById(Long id) {
        return folderRepository.findById(id)
                .map(folder -> {
                    folder.setBookmarks(bookmarkRepository.findByFolderId(folder.getId()));
                    return folder;
                });
    }

    public Folder createFolder(Folder folder) {
        if (folderRepository.existsByName(folder.getName())) {
            throw new IllegalArgumentException("Folder with name '" + folder.getName() + "' already exists");
        }
        return folderRepository.save(folder);
    }

    public Optional<Folder> updateFolder(Long id, Folder folderDetails) {
        return folderRepository.findById(id)
                .map(folder -> {
                    if (!folder.getName().equals(folderDetails.getName()) &&
                            folderRepository.existsByName(folderDetails.getName())) {
                        throw new IllegalArgumentException("Folder with name '" + folderDetails.getName() + "' already exists");
                    }
                    folder.setName(folderDetails.getName());
                    folder.setDescription(folderDetails.getDescription());
                    return folderRepository.save(folder);
                });
    }

    public void deleteFolder(Long id) {
        folderRepository.findById(id).ifPresent(folder -> {
            if (bookmarkRepository.countByFolderId(id) > 0) {
                throw new IllegalStateException("Cannot delete folder that contains bookmarks");
            }
            folderRepository.deleteById(id);
        });
    }

    public List<Folder> searchFolders(String searchTerm) {
        return folderRepository.searchFolders(searchTerm);
    }

    public List<Folder> getAllFoldersWithBookmarkCount() {
        return folderRepository.findAllWithBookmarkCount();
    }
}
