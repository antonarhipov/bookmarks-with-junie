import { BookmarkList } from './BookmarkList';

export class SearchBar {
    private input: HTMLInputElement;
    private bookmarkList: BookmarkList;
    private debounceTimeout: number | null = null;

    constructor(containerId: string, bookmarkList: BookmarkList) {
        const container = document.getElementById(containerId);
        if (!container) throw new Error(`Element with id ${containerId} not found`);

        const input = container.querySelector('input');
        if (!input) throw new Error('Search input not found');

        this.input = input;
        this.bookmarkList = bookmarkList;
        this.init();
    }

    private init() {
        this.setupEventListeners();
    }

    private setupEventListeners() {
        this.input.addEventListener('input', (e) => {
            const target = e.target as HTMLInputElement;
            this.handleSearch(target.value);
        });

        // Clear search on Escape key
        this.input.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.input.value = '';
                this.handleSearch('');
            }
        });
    }

    private handleSearch(query: string) {
        // Clear existing timeout
        if (this.debounceTimeout) {
            window.clearTimeout(this.debounceTimeout);
        }

        // Debounce search to avoid too many API calls
        this.debounceTimeout = window.setTimeout(() => {
            this.bookmarkList.setSearchQuery(query.trim() || null);
        }, 300);
    }

    // Public method to clear search
    public clear() {
        this.input.value = '';
        this.handleSearch('');
    }

    // Public method to set search value programmatically
    public setValue(value: string) {
        this.input.value = value;
        this.handleSearch(value);
    }
}