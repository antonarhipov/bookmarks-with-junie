import { Folder } from '../types';
import { folderApi } from '../api';

export class FolderView {
    private container: HTMLElement;
    private folders: Folder[] = [];
    private selectedFolderId: number | null = null;
    private onFolderSelect: (folderId: number) => void;

    constructor(containerId: string, onFolderSelect: (folderId: number) => void) {
        console.log('[DEBUG] Initializing FolderView with containerId:', containerId);

        // Debug DOM state
        console.log('[DEBUG] Initial DOM state:', {
            body: document.body.innerHTML,
            container: document.getElementById(containerId)?.outerHTML,
            allButtons: document.querySelectorAll('button').length
        });

        const element = document.getElementById(containerId);
        if (!element) throw new Error(`Element with id ${containerId} not found`);
        this.container = element;
        this.onFolderSelect = onFolderSelect;
        this.init();
    }

    private async init() {
        await this.loadFolders();
        this.setupEventListeners();
        this.render();
    }

    private async loadFolders() {
        try {
            this.folders = await folderApi.getAllFolders();
            this.render();
        } catch (error) {
            console.error('Error loading folders:', error);
        }
    }

    private setupEventListeners() {
        console.log('[DEBUG] Setting up FolderView event listeners');

        // Find the parent container and add button
        const parentContainer = this.container.parentElement;
        console.log('[DEBUG] Parent container:', {
            found: !!parentContainer,
            html: parentContainer?.innerHTML,
            buttons: parentContainer?.querySelectorAll('button').length
        });

        // Add new folder button
        const addButton = parentContainer?.querySelector('#add-folder-btn, button[data-action="add-folder"]');
        console.log('[DEBUG] Add button found:', !!addButton, addButton);

        if (addButton) {
            console.log('[DEBUG] Adding click listener to folder button');
            addButton.addEventListener('click', (e) => {
                console.log('[DEBUG] Folder button clicked', e);
                this.handleAddFolder();
            });
        } else {
            console.error('[DEBUG] Add folder button not found in the DOM');
        }

        // Folder selection
        this.container.addEventListener('click', (e) => {
            const target = e.target as HTMLElement;
            const folderItem = target.closest('[data-folder-id]');
            if (folderItem) {
                const folderId = Number(folderItem.getAttribute('data-folder-id'));
                this.selectFolder(folderId);
            }
        });

        // Context menu for folders (right click)
        this.container.addEventListener('contextmenu', (e) => {
            e.preventDefault();
            const target = e.target as HTMLElement;
            const folderItem = target.closest('[data-folder-id]');
            if (folderItem) {
                const folderId = Number(folderItem.getAttribute('data-folder-id'));
                this.showContextMenu(e, folderId);
            }
        });
    }

    private createFolderDialog() {
        console.log('[DEBUG] Creating folder dialog element');
        const existingDialog = document.getElementById('folder-dialog');
        if (existingDialog) {
            console.log('[DEBUG] Found existing dialog, removing it');
            existingDialog.remove();
        }

        const dialog = document.createElement('div');
        dialog.className = 'fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center hidden';
        dialog.id = 'folder-dialog';
        dialog.innerHTML = `
            <div class="bg-white rounded-lg p-6 w-full max-w-md">
                <h3 class="text-lg font-medium mb-4">New Folder</h3>
                <form id="folder-form" class="space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Name</label>
                        <input type="text" name="name" required
                               class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Description</label>
                        <textarea name="description" rows="3"
                                  class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"></textarea>
                    </div>
                    <div class="flex justify-end space-x-4">
                        <button type="button" class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200"
                                id="cancel-folder">Cancel</button>
                        <button type="submit" class="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700">
                            Create
                        </button>
                    </div>
                </form>
            </div>
        `;
        document.body.appendChild(dialog);
        console.log('[DEBUG] Dialog added to DOM:', document.getElementById('folder-dialog'));
        return dialog;
    }

    private setupFolderDialog() {
        console.log('[DEBUG] Setting up folder dialog');
        const dialog = this.createFolderDialog();
        console.log('[DEBUG] Dialog HTML:', dialog.innerHTML);

        const form = dialog.querySelector('form');
        const cancelBtn = dialog.querySelector('#cancel-folder');
        const submitBtn = dialog.querySelector('button[type="submit"]');
        console.log('[DEBUG] Dialog elements found:', {
            form: !!form,
            cancelBtn: !!cancelBtn,
            submitBtn: !!submitBtn,
            formHtml: form?.innerHTML
        });

        if (!form || !cancelBtn) {
            console.error('[DEBUG] Required dialog elements not found');
            return;
        }

        console.log('[DEBUG] Setting up dialog event listeners');

        // Close on cancel
        cancelBtn.addEventListener('click', () => {
            dialog.classList.add('hidden');
            form.reset();
        });

        // Close on outside click
        dialog.addEventListener('click', (e) => {
            const target = e.target as HTMLElement;
            if (target && target === dialog) {
                dialog.classList.add('hidden');
                form.reset();
            }
        });

        // Handle form submission
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(form);
            const folderData = {
                name: formData.get('name') as string,
                description: formData.get('description') as string || undefined
            };

            try {
                const newFolder = await folderApi.createFolder(folderData);
                this.folders.push(newFolder);
                this.render();
                dialog.classList.add('hidden');
                form.reset();
            } catch (error) {
                console.error('Error creating folder:', error);
                alert('Failed to create folder. Please try again.');
            }
        });
    }

    private async handleAddFolder() {
        console.log('[DEBUG] Add folder button clicked');
        console.log('[DEBUG] Current dialogs in DOM:', document.querySelectorAll('[id^="folder-dialog"]'));
        const dialog = document.getElementById('folder-dialog');
        if (!dialog) {
            console.log('[DEBUG] Creating new folder dialog');
            this.setupFolderDialog();
            const newDialog = document.getElementById('folder-dialog')!;
            newDialog.classList.remove('hidden');
            console.log('[DEBUG] Folder dialog shown');
        } else {
            console.log('[DEBUG] Showing existing folder dialog');
            dialog.classList.remove('hidden');
        }
    }

    private selectFolder(folderId: number) {
        this.selectedFolderId = folderId;
        this.render();
        this.onFolderSelect(folderId);
    }

    private async handleRenameFolder(folderId: number) {
        const folder = this.folders.find(f => f.id === folderId);
        if (!folder) return;

        const newName = prompt('Enter new folder name:', folder.name);
        if (!newName || newName === folder.name) return;

        try {
            const updatedFolder = await folderApi.updateFolder(folderId, { ...folder, name: newName });
            const index = this.folders.findIndex(f => f.id === folderId);
            if (index !== -1) {
                this.folders[index] = updatedFolder;
                this.render();
            }
        } catch (error) {
            console.error('Error renaming folder:', error);
        }
    }

    private async handleDeleteFolder(folderId: number) {
        const folder = this.folders.find(f => f.id === folderId);
        if (!folder) return;

        if (!confirm(`Are you sure you want to delete folder "${folder.name}"?`)) return;

        try {
            await folderApi.deleteFolder(folderId);
            this.folders = this.folders.filter(f => f.id !== folderId);
            if (this.selectedFolderId === folderId) {
                this.selectedFolderId = null;
            }
            this.render();
        } catch (error) {
            console.error('Error deleting folder:', error);
        }
    }

    private showContextMenu(event: MouseEvent, folderId: number) {
        const existingMenu = document.querySelector('.folder-context-menu');
        existingMenu?.remove();

        const menu = document.createElement('div');
        menu.className = 'folder-context-menu absolute bg-white shadow-lg rounded-md py-2 z-50';
        menu.style.left = `${event.pageX}px`;
        menu.style.top = `${event.pageY}px`;

        const menuItems = [
            { text: 'Rename', onClick: () => this.handleRenameFolder(folderId) },
            { text: 'Delete', onClick: () => this.handleDeleteFolder(folderId) }
        ];

        menuItems.forEach(item => {
            const menuItem = document.createElement('div');
            menuItem.className = 'px-4 py-2 hover:bg-gray-100 cursor-pointer';
            menuItem.textContent = item.text;
            menuItem.onclick = () => {
                item.onClick();
                menu.remove();
            };
            menu.appendChild(menuItem);
        });

        document.body.appendChild(menu);

        // Close menu on click outside
        const closeMenu = (e: MouseEvent) => {
            if (!menu.contains(e.target as Node)) {
                menu.remove();
                document.removeEventListener('click', closeMenu);
            }
        };
        setTimeout(() => document.addEventListener('click', closeMenu), 0);
    }

    private render() {
        this.container.innerHTML = this.folders
            .map(folder => `
                <div class="folder-item ${folder.id === this.selectedFolderId ? 'bg-blue-50' : ''}"
                     data-folder-id="${folder.id}">
                    <div class="flex items-center space-x-2 p-2 rounded hover:bg-gray-100 cursor-pointer">
                        <svg class="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                  d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/>
                        </svg>
                        <span class="text-sm">${folder.name}</span>
                    </div>
                </div>
            `)
            .join('');
    }
}
