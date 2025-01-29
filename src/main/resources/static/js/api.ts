import { Bookmark, Folder, BookmarkFilter, PaginatedResponse } from './types';

const API_BASE = '/api';

// Error handling helper
const handleResponse = async (response: Response) => {
    console.log('[DEBUG_LOG] API Response:', {
        url: response.url,
        status: response.status,
        ok: response.ok
    });

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log('[DEBUG_LOG] API Response Data:', data);
    return data;
};

// Folder API
export const folderApi = {
    getAllFolders: async (): Promise<Folder[]> => {
        const response = await fetch(`${API_BASE}/folders`);
        return handleResponse(response);
    },

    createFolder: async (folder: Folder): Promise<Folder> => {
        const response = await fetch(`${API_BASE}/folders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(folder)
        });
        return handleResponse(response);
    },

    updateFolder: async (id: number, folder: Folder): Promise<Folder> => {
        const response = await fetch(`${API_BASE}/folders/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(folder)
        });
        return handleResponse(response);
    },

    deleteFolder: async (id: number): Promise<void> => {
        const response = await fetch(`${API_BASE}/folders/${id}`, {
            method: 'DELETE'
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    },

    searchFolders: async (query: string): Promise<Folder[]> => {
        const response = await fetch(`${API_BASE}/folders/search?query=${encodeURIComponent(query)}`);
        return handleResponse(response);
    }
};

// Bookmark API
export const bookmarkApi = {
    getBookmarks: async (filter: BookmarkFilter): Promise<PaginatedResponse<Bookmark>> => {
        const params = new URLSearchParams();
        if (filter.search) params.append('search', filter.search);
        if (filter.sortBy) params.append('sortBy', filter.sortBy);
        if (filter.sortDirection) params.append('sortDir', filter.sortDirection);
        if (filter.page !== undefined) params.append('page', filter.page.toString());
        if (filter.size !== undefined) params.append('size', filter.size.toString());

        let endpoint = `${API_BASE}/bookmarks`;
        if (filter.folderId) {
            endpoint = `${API_BASE}/bookmarks/folder/${filter.folderId}`;
        }

        const url = `${endpoint}?${params.toString()}`;
        console.log('[DEBUG_LOG] Fetching bookmarks:', {
            url,
            filter
        });

        try {
            const response = await fetch(url);
            return handleResponse(response);
        } catch (error) {
            console.error('[DEBUG_LOG] Error fetching bookmarks:', {
                error,
                url,
                filter
            });
            throw error;
        }
    },

    createBookmark: async (bookmark: Bookmark): Promise<Bookmark> => {
        const response = await fetch(`${API_BASE}/bookmarks`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bookmark)
        });
        return handleResponse(response);
    },

    updateBookmark: async (id: number, bookmark: Bookmark): Promise<Bookmark> => {
        const response = await fetch(`${API_BASE}/bookmarks/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bookmark)
        });
        return handleResponse(response);
    },

    deleteBookmark: async (id: number): Promise<void> => {
        const response = await fetch(`${API_BASE}/bookmarks/${id}`, {
            method: 'DELETE'
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    },

    deleteBookmarks: async (ids: number[]): Promise<void> => {
        const response = await fetch(`${API_BASE}/bookmarks/bulk`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids)
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    },

    searchBookmarks: async (query: string): Promise<Bookmark[]> => {
        const response = await fetch(`${API_BASE}/bookmarks/search?query=${encodeURIComponent(query)}`);
        return handleResponse(response);
    }
};
