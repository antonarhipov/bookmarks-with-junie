package org.crud.bookmarks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.crud.bookmarks.Folder;
import org.crud.bookmarks.service.FolderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FolderController.class)
class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FolderService folderService;

    @Autowired
    private ObjectMapper objectMapper;

    private Folder testFolder;

    @BeforeEach
    void setUp() {
        testFolder = new Folder("Test Folder");
        testFolder.setId(1L);
        testFolder.setDescription("Test Description");
    }

    @Test
    void getAllFolders_ShouldReturnFolders() throws Exception {
        when(folderService.getAllFolders()).thenReturn(Arrays.asList(testFolder));

        mockMvc.perform(get("/api/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(testFolder.getName()))
                .andExpect(jsonPath("$[0].description").value(testFolder.getDescription()));
    }

    @Test
    void getFolderById_WhenExists_ShouldReturnFolder() throws Exception {
        when(folderService.getFolderById(1L)).thenReturn(Optional.of(testFolder));

        mockMvc.perform(get("/api/folders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testFolder.getName()))
                .andExpect(jsonPath("$.description").value(testFolder.getDescription()));
    }

    @Test
    void getFolderById_WhenNotExists_ShouldReturn404() throws Exception {
        when(folderService.getFolderById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/folders/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createFolder_WithValidData_ShouldCreateFolder() throws Exception {
        when(folderService.createFolder(any(Folder.class))).thenReturn(testFolder);

        mockMvc.perform(post("/api/folders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testFolder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testFolder.getName()));
    }

    @Test
    void createFolder_WithDuplicateName_ShouldReturn400() throws Exception {
        when(folderService.createFolder(any(Folder.class)))
                .thenThrow(new IllegalArgumentException("Folder with name 'Test Folder' already exists"));

        mockMvc.perform(post("/api/folders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testFolder)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFolder_WhenExists_ShouldUpdateFolder() throws Exception {
        when(folderService.updateFolder(eq(1L), any(Folder.class)))
                .thenReturn(Optional.of(testFolder));

        mockMvc.perform(put("/api/folders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testFolder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testFolder.getName()));
    }

    @Test
    void updateFolder_WhenNotExists_ShouldReturn404() throws Exception {
        when(folderService.updateFolder(eq(1L), any(Folder.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/folders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testFolder)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFolder_WithNoBookmarks_ShouldDeleteFolder() throws Exception {
        mockMvc.perform(delete("/api/folders/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFolder_WithBookmarks_ShouldReturn400() throws Exception {
        doThrow(new IllegalStateException("Cannot delete folder that contains bookmarks"))
                .when(folderService).deleteFolder(1L);

        mockMvc.perform(delete("/api/folders/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchFolders_ShouldReturnMatchingFolders() throws Exception {
        when(folderService.searchFolders("test"))
                .thenReturn(Arrays.asList(testFolder));

        mockMvc.perform(get("/api/folders/search").param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(testFolder.getName()));
    }
}