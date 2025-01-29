import { FolderView } from './components/FolderView.js';
import { BookmarkList } from './components/BookmarkList.js';
import { SearchBar } from './components/SearchBar.js';
import { MultiSelect } from './components/MultiSelect.js';
import { BookmarkForm } from './components/BookmarkForm.js';

console.log('[DEBUG_LOG] Main script loaded');

document.addEventListener('DOMContentLoaded', () => {
    console.log('[DEBUG_LOG] DOMContentLoaded event fired');
    console.log('[DEBUG_LOG] Initializing components...');

    // Verify DOM elements exist
    const folderTreeElement = document.getElementById('folder-tree');
    const bookmarkListElement = document.getElementById('bookmark-list');
    const searchBarElement = document.getElementById('search-bar');

    console.log('[DEBUG] DOM elements found:', {
        folderTree: !!folderTreeElement,
        bookmarkList: !!bookmarkListElement,
        searchBar: !!searchBarElement
    });

    // Initialize components
    console.log('[DEBUG] Creating MultiSelect...');
    const multiSelect = new MultiSelect('select-all', 'delete-selected', (state) => {
        // MultiSelect handles visibility internally
    });

    console.log('[DEBUG] Creating BookmarkList...');
    const bookmarkList = new BookmarkList('bookmark-list', multiSelect);
    console.log('[DEBUG] Creating SearchBar...');
    const searchBar = new SearchBar('search-bar', bookmarkList);

    console.log('[DEBUG] Creating FolderView...');
    try {
        // Initialize folder view with bookmark list update callback
        const folderView = new FolderView('folder-tree', (folderId: number | null) => {
            bookmarkList.setFolderId(folderId);
            searchBar.clear(); // Clear search when changing folders
        });
        console.log('[DEBUG] FolderView created successfully');
    } catch (error) {
        console.error('[DEBUG] Error creating FolderView:', error);
    }

    // Initialize bookmark form
    const bookmarkForm = new BookmarkForm(async (bookmark) => {
        console.log('[DEBUG] Bookmark saved successfully:', bookmark);
        // Add a small delay to ensure transaction is complete
        await new Promise(resolve => setTimeout(resolve, 100));
        console.log('[DEBUG] Reloading bookmark list...');
        await bookmarkList.resetAndReload();
        console.log('[DEBUG] Bookmark list reloaded');
    });

    // Add event listener for new bookmark button
    const newBookmarkBtn = document.getElementById('new-bookmark');
    if (newBookmarkBtn) {
        newBookmarkBtn.addEventListener('click', () => {
            bookmarkForm.show();
        });
    }

    // Add event listener for the delete selected button
    const deleteSelectedBtn = document.getElementById('delete-selected');
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', async () => {
            const selectedIds = Array.from(multiSelect.getSelectedIds());
            if (selectedIds.length === 0) return;

            if (confirm(`Are you sure you want to delete ${selectedIds.length} bookmarks?`)) {
                try {
                    await fetch('/api/bookmarks/bulk', {
                        method: 'DELETE',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify(selectedIds),
                    });

                    // Refresh the bookmark list
                    bookmarkList.resetAndReload();
                    multiSelect.clearSelection();
                } catch (error) {
                    console.error('Error deleting bookmarks:', error);
                }
            }
        });
    }
});
