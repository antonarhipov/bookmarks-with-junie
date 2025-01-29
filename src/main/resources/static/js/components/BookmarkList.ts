import { Bookmark, BookmarkFilter, SelectionState } from '../types';
import { bookmarkApi } from '../api';

function escapeHtml(unsafe: string): string {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

export class BookmarkList {
    private container: HTMLElement;
    private bookmarks: Bookmark[] = [];
    private currentFilter: BookmarkFilter = {
        page: 0,
        size: 20,
        sortBy: 'title',
        sortDirection: 'asc'
    };
    private loading = false;
    private hasMore = true;
    private selection: SelectionState = {
        selectedIds: new Set(),
        isAllSelected: false
    };
    private intersectionObserver!: IntersectionObserver;

    constructor(containerId: string) {
        console.log('[DEBUG_LOG] Initializing BookmarkList with containerId:', containerId);
        const element = document.getElementById(containerId);
        console.log('[DEBUG_LOG] Found container element:', !!element);
        if (!element) throw new Error(`Element with id ${containerId} not found`);
        this.container = element;
        this.setupIntersectionObserver();
        this.init();
    }

    private async init() {
        console.log('[DEBUG_LOG] BookmarkList init started');
        this.setupEventListeners();
        console.log('[DEBUG_LOG] Event listeners set up, loading bookmarks...');
        await this.loadBookmarks();
        console.log('[DEBUG_LOG] BookmarkList init completed');
    }

    private setupIntersectionObserver() {
        this.intersectionObserver = new IntersectionObserver(
            (entries) => {
                const lastEntry = entries[0];
                if (lastEntry.isIntersecting && !this.loading && this.hasMore) {
                    this.loadMore();
                }
            },
            { threshold: 0.5 }
        );
    }

    private setupEventListeners() {
        // Sort dropdown
        const sortSelect = document.getElementById('sort-by');
        if (sortSelect) {
            sortSelect.addEventListener('change', (e) => {
                const target = e.target as HTMLSelectElement;
                this.currentFilter.sortBy = target.value as 'title' | 'createdAt' | 'url';
                this.resetAndReload();
            });
        }

        // Select all button
        const selectAllBtn = document.getElementById('select-all');
        if (selectAllBtn) {
            selectAllBtn.addEventListener('click', () => this.toggleSelectAll());
        }

        // Delete selected button
        const deleteSelectedBtn = document.getElementById('delete-selected');
        if (deleteSelectedBtn) {
            deleteSelectedBtn.addEventListener('click', () => this.deleteSelected());
        }
    }

    public setFolderId(folderId: number | null) {
        this.currentFilter.folderId = folderId || undefined;
        this.resetAndReload();
    }

    public setSearchQuery(query: string | null) {
        this.currentFilter.search = query || undefined;
        this.resetAndReload();
    }

    public async resetAndReload() {
        console.log('[DEBUG] Starting resetAndReload');
        this.bookmarks = [];
        this.currentFilter.page = 0;
        this.hasMore = true;
        this.loading = false;
        this.selection = { selectedIds: new Set(), isAllSelected: false };
        console.log('[DEBUG] Reset state - currentFilter:', this.currentFilter);
        await this.loadBookmarks();
        console.log('[DEBUG] Completed resetAndReload');
    }

    private async loadBookmarks() {
        console.log('[DEBUG_LOG] loadBookmarks called', {
            loading: this.loading,
            hasMore: this.hasMore,
            currentFilter: this.currentFilter,
            bookmarksCount: this.bookmarks.length
        });

        if (this.loading || !this.hasMore) {
            console.log('[DEBUG_LOG] Skipping loadBookmarks - loading:', this.loading, 'hasMore:', this.hasMore);
            return;
        }

        console.log('[DEBUG_LOG] Loading bookmarks with filter:', this.currentFilter);
        this.loading = true;
        try {
            const response = await bookmarkApi.getBookmarks(this.currentFilter);
            console.log('[DEBUG_LOG] API Response received:', {
                totalElements: response.totalElements,
                totalPages: response.totalPages,
                contentLength: response.content.length,
                currentPage: response.number
            });

            // On initial load or reset, replace the bookmarks array
            if (this.currentFilter.page === 0) {
                this.bookmarks = response.content;
            } else {
                // For subsequent loads (pagination), append to the existing array
                this.bookmarks = [...this.bookmarks, ...response.content];
            }

            this.hasMore = this.currentFilter.page! < response.totalPages - 1;
            this.currentFilter.page! += 1;

            console.log('[DEBUG_LOG] Updated bookmarks state:', {
                totalBookmarks: this.bookmarks.length,
                hasMore: this.hasMore,
                nextPage: this.currentFilter.page
            });
            this.render();
        } catch (error) {
            console.error('Error loading bookmarks:', error);
        } finally {
            this.loading = false;
        }
    }

    private async loadMore() {
        await this.loadBookmarks();
    }

    private toggleSelectAll() {
        if (this.selection.isAllSelected) {
            this.selection = { selectedIds: new Set(), isAllSelected: false };
        } else {
            const allIds = this.bookmarks.map(b => b.id!);
            this.selection = { selectedIds: new Set(allIds), isAllSelected: true };
        }
        this.updateDeleteButton();
        this.render();
    }

    private toggleBookmarkSelection(bookmarkId: number) {
        if (this.selection.selectedIds.has(bookmarkId)) {
            this.selection.selectedIds.delete(bookmarkId);
        } else {
            this.selection.selectedIds.add(bookmarkId);
        }
        this.selection.isAllSelected = this.selection.selectedIds.size === this.bookmarks.length;
        this.updateDeleteButton();
        this.render();
    }

    private updateDeleteButton() {
        const deleteBtn = document.getElementById('delete-selected');
        if (deleteBtn) {
            if (this.selection.selectedIds.size > 0) {
                deleteBtn.classList.remove('hidden');
            } else {
                deleteBtn.classList.add('hidden');
            }
        }
    }

    private async deleteSelected() {
        if (this.selection.selectedIds.size === 0) return;

        if (!confirm(`Are you sure you want to delete ${this.selection.selectedIds.size} bookmarks?`)) return;

        try {
            await bookmarkApi.deleteBookmarks(Array.from(this.selection.selectedIds));
            await this.resetAndReload();
        } catch (error) {
            console.error('Error deleting bookmarks:', error);
        }
    }

    private render() {
        console.log('[DEBUG_LOG] Starting render with container:', {
            containerId: this.container.id,
            containerVisible: this.container.offsetParent !== null,
            containerDimensions: {
                width: this.container.offsetWidth,
                height: this.container.offsetHeight
            }
        });

        console.log('[DEBUG_LOG] Rendering bookmarks:', {
            count: this.bookmarks.length,
            bookmarks: this.bookmarks
        });

        try {
            const bookmarkElements = this.bookmarks.map(bookmark => {
                if (!bookmark.title || !bookmark.url) {
                    console.error('[DEBUG_LOG] Invalid bookmark data:', bookmark);
                    return '';
                }

                return `
                    <div class="bookmark-item p-4 hover:bg-gray-50 ${
                        this.selection.selectedIds.has(bookmark.id!) ? 'bg-blue-50' : ''
                    }" data-bookmark-id="${bookmark.id}">
                        <div class="flex items-center space-x-4">
                            <input type="checkbox" 
                                   class="h-4 w-4 text-blue-600 rounded border-gray-300"
                                   ${this.selection.selectedIds.has(bookmark.id!) ? 'checked' : ''}>
                            <div class="flex-1">
                                <h3 class="text-lg font-medium">
                                    <a href="${escapeHtml(bookmark.url)}" target="_blank" class="text-blue-600 hover:underline">
                                        ${escapeHtml(bookmark.title)}
                                    </a>
                                </h3>
                                ${bookmark.description ? `<p class="text-gray-600">${escapeHtml(bookmark.description)}</p>` : ''}
                                <div class="text-sm text-gray-500 mt-1">
                                    ${bookmark.createdAt ? new Date(bookmark.createdAt).toLocaleDateString() : 'No date'}
                                </div>
                            </div>
                        </div>
                    </div>
                `;
            }).filter(html => html.length > 0).join('');

            console.log('[DEBUG_LOG] Generated HTML:', {
                length: bookmarkElements.length,
                isEmpty: bookmarkElements.length === 0,
                bookmarksCount: this.bookmarks.length
            });

            if (bookmarkElements.length === 0) {
                this.container.innerHTML = '<div class="p-4 text-gray-500">No bookmarks found</div>';
            } else {
                this.container.innerHTML = bookmarkElements;
            }
        } catch (error) {
            console.error('[DEBUG_LOG] Error generating bookmark HTML:', error);
            this.container.innerHTML = '<div class="p-4 text-red-500">Error displaying bookmarks</div>';
        }
        console.log('[DEBUG_LOG] Container updated, new content length:', this.container.children.length);

        // Add click handlers for checkboxes
        this.container.querySelectorAll('.bookmark-item').forEach(item => {
            const bookmarkId = Number(item.getAttribute('data-bookmark-id'));
            const checkbox = item.querySelector('input[type="checkbox"]');
            if (checkbox) {
                checkbox.addEventListener('change', () => this.toggleBookmarkSelection(bookmarkId));
            }
        });

        // Setup intersection observer for the last item
        const lastItem = this.container.lastElementChild;
        console.log('[DEBUG_LOG] Setting up intersection observer:', {
            hasLastItem: !!lastItem,
            totalItems: this.container.children.length
        });

        if (lastItem) {
            this.intersectionObserver.observe(lastItem);
        }
    }
}
