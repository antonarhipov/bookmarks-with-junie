import { Bookmark, BookmarkFilter, SelectionState } from '../types';
import { bookmarkApi } from '../api';

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
        const element = document.getElementById(containerId);
        if (!element) throw new Error(`Element with id ${containerId} not found`);
        this.container = element;
        this.setupIntersectionObserver();
        this.init();
    }

    private async init() {
        this.setupEventListeners();
        await this.loadBookmarks();
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
        if (this.loading || !this.hasMore) {
            console.log('[DEBUG] Skipping loadBookmarks - loading:', this.loading, 'hasMore:', this.hasMore);
            return;
        }

        console.log('[DEBUG] Loading bookmarks with filter:', this.currentFilter);
        this.loading = true;
        try {
            const response = await bookmarkApi.getBookmarks(this.currentFilter);
            console.log('[DEBUG] Loaded bookmarks:', response);
            this.bookmarks = [...this.bookmarks, ...response.content];
            this.hasMore = this.currentFilter.page! < response.totalPages - 1;
            this.currentFilter.page! += 1;
            console.log('[DEBUG] Updated bookmarks array:', this.bookmarks.length, 'items');
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
        const bookmarkElements = this.bookmarks.map(bookmark => `
            <div class="bookmark-item p-4 hover:bg-gray-50 ${
                this.selection.selectedIds.has(bookmark.id!) ? 'bg-blue-50' : ''
            }" data-bookmark-id="${bookmark.id}">
                <div class="flex items-center space-x-4">
                    <input type="checkbox" 
                           class="h-4 w-4 text-blue-600 rounded border-gray-300"
                           ${this.selection.selectedIds.has(bookmark.id!) ? 'checked' : ''}>
                    <div class="flex-1">
                        <h3 class="text-lg font-medium">
                            <a href="${bookmark.url}" target="_blank" class="text-blue-600 hover:underline">
                                ${bookmark.title}
                            </a>
                        </h3>
                        ${bookmark.description ? `<p class="text-gray-600">${bookmark.description}</p>` : ''}
                        <div class="text-sm text-gray-500 mt-1">
                            ${new Date(bookmark.createdAt!).toLocaleDateString()}
                        </div>
                    </div>
                </div>
            </div>
        `).join('');

        this.container.innerHTML = bookmarkElements;

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
        if (lastItem) {
            this.intersectionObserver.observe(lastItem);
        }
    }
}
