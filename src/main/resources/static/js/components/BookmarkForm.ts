import { Bookmark, Folder } from '../types';
import { bookmarkApi, folderApi } from '../api';

export class BookmarkForm {
    private modal!: HTMLDivElement;
    private form!: HTMLFormElement;
    private currentBookmark: Bookmark | null = null;
    private onSave: (bookmark: Bookmark) => void;

    constructor(onSave: (bookmark: Bookmark) => void) {
        this.onSave = onSave;
        this.createModal();
        this.setupEventListeners();
    }

    private createModal() {
        this.modal = document.createElement('div');
        this.modal.className = 'fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center hidden';
        this.modal.innerHTML = `
            <div class="bg-white rounded-lg p-8 w-full max-w-md">
                <h2 class="text-2xl font-bold mb-6">Add Bookmark</h2>
                <form id="bookmark-form" class="space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Title</label>
                        <input type="text" name="title" required
                               class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">URL</label>
                        <input type="url" name="url" required
                               class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Description</label>
                        <textarea name="description" rows="3"
                                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"></textarea>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Folder</label>
                        <select name="folderId"
                                class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                            <option value="">No Folder</option>
                        </select>
                    </div>
                    <div class="flex justify-end space-x-4 mt-6">
                        <button type="button" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
                                id="cancel-bookmark">Cancel</button>
                        <button type="submit" class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
                            Save
                        </button>
                    </div>
                </form>
            </div>
        `;
        document.body.appendChild(this.modal);
        this.form = this.modal.querySelector('form')!;
    }

    private setupEventListeners() {
        // Close modal on cancel
        const cancelButton = this.modal.querySelector('#cancel-bookmark');
        if (cancelButton) {
            cancelButton.addEventListener('click', () => this.hide());
        }

        // Close modal on outside click
        this.modal.addEventListener('click', (e) => {
            if (e.target === this.modal) {
                this.hide();
            }
        });

        // Form submission
        this.form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(this.form);
            const bookmark: Bookmark = {
                title: formData.get('title') as string,
                url: formData.get('url') as string,
                description: formData.get('description') as string || undefined,
                folderId: formData.get('folderId') ? Number(formData.get('folderId')) : undefined
            };

            try {
                const savedBookmark = this.currentBookmark
                    ? await bookmarkApi.updateBookmark(this.currentBookmark.id!, bookmark)
                    : await bookmarkApi.createBookmark(bookmark);

                this.onSave(savedBookmark);
                this.hide();
                this.form.reset();
            } catch (error) {
                console.error('Error saving bookmark:', error);
                alert('Failed to save bookmark. Please try again.');
            }
        });
    }

    public async show(bookmark?: Bookmark) {
        this.currentBookmark = bookmark || null;

        // Update form title
        const title = this.modal.querySelector('h2');
        if (title) {
            title.textContent = bookmark ? 'Edit Bookmark' : 'Add Bookmark';
        }

        // Load folders for the select
        try {
            const folders = await folderApi.getAllFolders();
            const select = this.form.querySelector('select[name="folderId"]');
            if (select) {
                select.innerHTML = '<option value="">No Folder</option>' +
                    folders.map(folder => `
                        <option value="${folder.id}" ${bookmark?.folderId === folder.id ? 'selected' : ''}>
                            ${folder.name}
                        </option>
                    `).join('');
            }
        } catch (error) {
            console.error('Error loading folders:', error);
        }

        // Fill form if editing
        if (bookmark) {
            const { title, url, description, folderId } = bookmark;
            const form = this.form;
            form.querySelector<HTMLInputElement>('input[name="title"]')!.value = title;
            form.querySelector<HTMLInputElement>('input[name="url"]')!.value = url;
            form.querySelector<HTMLTextAreaElement>('textarea[name="description"]')!.value = description || '';
            if (folderId) {
                form.querySelector<HTMLSelectElement>('select[name="folderId"]')!.value = folderId.toString();
            }
        }

        this.modal.classList.remove('hidden');
    }

    public hide() {
        this.modal.classList.add('hidden');
        this.form.reset();
        this.currentBookmark = null;
    }
}
