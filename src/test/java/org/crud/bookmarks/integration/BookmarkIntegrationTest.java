package org.crud.bookmarks.integration;

import org.crud.bookmarks.Bookmark;
import org.crud.bookmarks.Folder;
import org.crud.bookmarks.controller.BookmarkController;
import org.crud.bookmarks.repository.BookmarkRepository;
import org.crud.bookmarks.repository.FolderRepository;
import org.crud.bookmarks.service.BookmarkService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.core.ParameterizedTypeReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BookmarkIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void createBookmark_ShouldBeImmediatelyAvailable() throws InterruptedException {
        // Create a new bookmark
        Bookmark newBookmark = new Bookmark("Test Bookmark", "https://test.com");
        newBookmark.setDescription("Test Description");

        // Save the bookmark
        ResponseEntity<Bookmark> createResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/bookmarks",
            newBookmark,
            Bookmark.class
        );

        // Verify creation was successful
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().getId());

        // Immediately try to fetch all bookmarks
        ResponseEntity<Bookmark[]> getAllResponse = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/bookmarks",
            Bookmark[].class
        );

        // Verify the bookmark is immediately available
        assertEquals(HttpStatus.OK, getAllResponse.getStatusCode());
        assertNotNull(getAllResponse.getBody());
        assertTrue(getAllResponse.getBody().length > 0);

        // Find our newly created bookmark
        boolean found = false;
        for (Bookmark bookmark : getAllResponse.getBody()) {
            if (bookmark.getId().equals(createResponse.getBody().getId())) {
                found = true;
                assertEquals(newBookmark.getTitle(), bookmark.getTitle());
                assertEquals(newBookmark.getUrl(), bookmark.getUrl());
                assertEquals(newBookmark.getDescription(), bookmark.getDescription());
                break;
            }
        }
        assertTrue(found, "Newly created bookmark should be immediately available in the list");
    }

    @AfterEach
    void cleanup() {
        bookmarkRepository.deleteAll();
        folderRepository.deleteAll();
    }

    @Test
    void getBookmarksByFolder_ShouldReturnOnlyBookmarksInFolder() {
        // Create a test folder
        Folder folder = new Folder();
        folder.setName("Test Folder");
        folder.setDescription("Test Folder Description");
        ResponseEntity<Folder> folderResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/folders",
            folder,
            Folder.class
        );
        assertEquals(HttpStatus.OK, folderResponse.getStatusCode());
        assertNotNull(folderResponse.getBody());
        Long folderId = folderResponse.getBody().getId();

        // Create bookmarks in a specific folder
        Bookmark bookmarkInFolder = new Bookmark("Bookmark in Folder", "https://test1.com");
        bookmarkInFolder.setFolderId(folderId);
        bookmarkInFolder.setDescription("Test Description 1");

        Bookmark bookmarkNotInFolder = new Bookmark("Bookmark not in Folder", "https://test2.com");
        bookmarkNotInFolder.setDescription("Test Description 2");

        // Save both bookmarks
        ResponseEntity<Bookmark> createResponse1 = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/bookmarks",
            bookmarkInFolder,
            Bookmark.class
        );
        ResponseEntity<Bookmark> createResponse2 = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/bookmarks",
            bookmarkNotInFolder,
            Bookmark.class
        );

        assertEquals(HttpStatus.OK, createResponse1.getStatusCode());
        assertEquals(HttpStatus.OK, createResponse2.getStatusCode());

        // Get bookmarks for the specific folder
        ResponseEntity<PageResponse<Bookmark>> getFolderResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/bookmarks/folder/" + folderId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<PageResponse<Bookmark>>() {}
        );

        // Verify only bookmarks from the folder are returned
        assertEquals(HttpStatus.OK, getFolderResponse.getStatusCode());
        assertNotNull(getFolderResponse.getBody());
        assertEquals(1, getFolderResponse.getBody().getContent().size());
        Bookmark returnedBookmark = getFolderResponse.getBody().getContent().get(0);
        assertEquals(bookmarkInFolder.getTitle(), returnedBookmark.getTitle());
        assertEquals(folderId, returnedBookmark.getFolderId());
    }
}
