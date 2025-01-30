import { Bookmark, BookmarkFilter, SelectionState } from '../types';
import { bookmarkApi } from '../api';
import { MultiSelect } from './MultiSelect';

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
    private multiSelect: MultiSelect;
    private intersectionObserver!: IntersectionObserver;

    constructor(containerId: string, multiSelect: MultiSelect) {
        console.log('[DEBUG_LOG] Initializing BookmarkList with containerId:', containerId);
        const element = document.getElementById(containerId);
        console.log('[DEBUG_LOG] Found container element:', !!element);
        if (!element) throw new Error(`Element with id ${containerId} not found`);
        this.container = element;
        this.multiSelect = multiSelect;
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
        this.multiSelect.clearSelection();
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

            // Update MultiSelect with current bookmark IDs
            this.multiSelect.setAvailableItems(this.bookmarks.map(b => b.id!));

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

    private toggleBookmarkSelection(bookmarkId: number) {
        this.multiSelect.toggleSelection(bookmarkId);
        this.render();
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
                        this.multiSelect.isSelected(bookmark.id!) ? 'bg-blue-50' : ''
                    }" data-bookmark-id="${bookmark.id}">
                        <div class="flex items-center space-x-4">
                            <input type="checkbox" 
                                   class="h-4 w-4 text-blue-600 rounded border-gray-300"
                                   ${this.multiSelect.isSelected(bookmark.id!) ? 'checked' : ''}>
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
