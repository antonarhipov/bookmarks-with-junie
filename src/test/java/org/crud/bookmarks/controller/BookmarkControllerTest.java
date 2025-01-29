package org.crud.bookmarks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.crud.bookmarks.Bookmark;
import org.crud.bookmarks.service.BookmarkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookmarkController.class)
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookmarkService bookmarkService;

    @Autowired
    private ObjectMapper objectMapper;

    private Bookmark testBookmark;

    @BeforeEach
    void setUp() {
        testBookmark = new Bookmark("Test Bookmark", "https://test.com");
        testBookmark.setId(1L);
        testBookmark.setDescription("Test Description");
    }

    @Test
    void getAllBookmarks_ShouldReturnBookmarks() throws Exception {
        when(bookmarkService.getAllBookmarks()).thenReturn(Arrays.asList(testBookmark));

        mockMvc.perform(get("/api/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(testBookmark.getTitle()))
                .andExpect(jsonPath("$[0].url").value(testBookmark.getUrl()));
    }

    @Test
    void getBookmarkById_WhenExists_ShouldReturnBookmark() throws Exception {
        when(bookmarkService.getBookmarkById(1L)).thenReturn(Optional.of(testBookmark));

        mockMvc.perform(get("/api/bookmarks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testBookmark.getTitle()))
                .andExpect(jsonPath("$.url").value(testBookmark.getUrl()));
    }

    @Test
    void getBookmarkById_WhenNotExists_ShouldReturn404() throws Exception {
        when(bookmarkService.getBookmarkById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookmarks/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBookmark_WithValidData_ShouldCreateBookmark() throws Exception {
        when(bookmarkService.createBookmark(any(Bookmark.class))).thenReturn(testBookmark);

        mockMvc.perform(post("/api/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBookmark)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testBookmark.getTitle()))
                .andExpect(jsonPath("$.url").value(testBookmark.getUrl()));
    }

    @Test
    void updateBookmark_WhenExists_ShouldUpdateBookmark() throws Exception {
        when(bookmarkService.updateBookmark(eq(1L), any(Bookmark.class)))
                .thenReturn(Optional.of(testBookmark));

        mockMvc.perform(put("/api/bookmarks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBookmark)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testBookmark.getTitle()));
    }

    @Test
    void updateBookmark_WhenNotExists_ShouldReturn404() throws Exception {
        when(bookmarkService.updateBookmark(eq(1L), any(Bookmark.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/bookmarks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBookmark)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBookmark_ShouldDeleteBookmark() throws Exception {
        mockMvc.perform(delete("/api/bookmarks/1"))
                .andExpect(status().isOk());
    }

    @Test
    void searchBookmarks_ShouldReturnMatchingBookmarks() throws Exception {
        Page<Bookmark> page = new PageImpl<>(Arrays.asList(testBookmark));
        when(bookmarkService.searchBookmarks(eq("test"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/bookmarks/search").param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(testBookmark.getTitle()));
    }
}
