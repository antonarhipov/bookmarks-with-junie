package org.crud.bookmarks.service;

import org.crud.bookmarks.Folder;
import org.crud.bookmarks.Bookmark;
import org.crud.bookmarks.repository.BookmarkRepository;
import org.crud.bookmarks.repository.FolderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private FolderService folderService;

    private Folder testFolder;
    private Bookmark testBookmark;

    @BeforeEach
    void setUp() {
        testFolder = new Folder("Test Folder");
        testFolder.setId(1L);
        testFolder.setDescription("Test Description");

        testBookmark = new Bookmark("Test Bookmark", "https://test.com");
        testBookmark.setFolderId(1L);
    }

    @Test
    void getAllFolders_ShouldReturnAllFolders() {
        List<Folder> folders = Arrays.asList(testFolder);
        when(folderRepository.findAllOrderByName()).thenReturn(folders);

        List<Folder> result = folderService.getAllFolders();

        assertEquals(folders.size(), result.size());
        assertEquals(folders.get(0).getName(), result.get(0).getName());
    }

    @Test
    void getFolderById_WhenExists_ShouldReturnFolder() {
        List<Bookmark> bookmarks = Arrays.asList(testBookmark);
        when(folderRepository.findById(1L)).thenReturn(Optional.of(testFolder));
        when(bookmarkRepository.findByFolderId(1L)).thenReturn(bookmarks);

        Optional<Folder> result = folderService.getFolderById(1L);

        assertTrue(result.isPresent());
        assertEquals(testFolder.getName(), result.get().getName());
        assertEquals(1, result.get().getBookmarks().size());
    }

    @Test
    void createFolder_WithValidData_ShouldCreateFolder() {
        when(folderRepository.existsByName(testFolder.getName())).thenReturn(false);
        when(folderRepository.save(any(Folder.class))).thenReturn(testFolder);

        Folder result = folderService.createFolder(testFolder);

        assertNotNull(result);
        assertEquals(testFolder.getName(), result.getName());
        verify(folderRepository).save(any(Folder.class));
    }

    @Test
    void createFolder_WithDuplicateName_ShouldThrowException() {
        when(folderRepository.existsByName(testFolder.getName())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
            folderService.createFolder(testFolder)
        );
    }

    @Test
    void updateFolder_WhenExists_ShouldUpdateFolder() {
        Folder updatedFolder = new Folder("Updated Folder");
        when(folderRepository.findById(1L)).thenReturn(Optional.of(testFolder));
        when(folderRepository.existsByName(updatedFolder.getName())).thenReturn(false);
        when(folderRepository.save(any(Folder.class))).thenReturn(updatedFolder);

        Optional<Folder> result = folderService.updateFolder(1L, updatedFolder);

        assertTrue(result.isPresent());
        assertEquals(updatedFolder.getName(), result.get().getName());
    }

    @Test
    void deleteFolder_WithNoBookmarks_ShouldDeleteFolder() {
        when(folderRepository.findById(1L)).thenReturn(Optional.of(testFolder));
        when(bookmarkRepository.countByFolderId(1L)).thenReturn(0);

        folderService.deleteFolder(1L);

        verify(folderRepository).deleteById(1L);
    }

    @Test
    void deleteFolder_WithBookmarks_ShouldThrowException() {
        when(folderRepository.findById(1L)).thenReturn(Optional.of(testFolder));
        when(bookmarkRepository.countByFolderId(1L)).thenReturn(1);

        assertThrows(IllegalStateException.class, () ->
            folderService.deleteFolder(1L)
        );
    }

    @Test
    void searchFolders_ShouldReturnMatchingFolders() {
        List<Folder> folders = Arrays.asList(testFolder);
        when(folderRepository.searchFolders("Test")).thenReturn(folders);

        List<Folder> result = folderService.searchFolders("Test");

        assertEquals(folders.size(), result.size());
        assertEquals(folders.get(0).getName(), result.get(0).getName());
    }
}
