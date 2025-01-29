// Types for the bookmark manager application

export interface Bookmark {
    id?: number;
    title: string;
    description?: string;
    url: string;
    folderId?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface Folder {
    id?: number;
    name: string;
    description?: string;
    bookmarks?: Bookmark[];
    createdAt?: string;
    updatedAt?: string;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface BookmarkFilter {
    folderId?: number;
    search?: string;
    sortBy?: 'title' | 'createdAt' | 'url';
    sortDirection?: 'asc' | 'desc';
    page?: number;
    size?: number;
}

export interface SelectionState {
    selectedIds: Set<number>;
    isAllSelected: boolean;
}