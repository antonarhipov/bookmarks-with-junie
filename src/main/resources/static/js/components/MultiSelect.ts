import { SelectionState } from '../types';

export class MultiSelect {
    private selectionState: SelectionState = {
        selectedIds: new Set(),
        isAllSelected: false
    };
    private selectAllButton: HTMLElement;
    private deleteButton: HTMLElement;
    private onSelectionChange: (state: SelectionState) => void;

    constructor(
        selectAllButtonId: string,
        deleteButtonId: string,
        onSelectionChange: (state: SelectionState) => void
    ) {
        const selectAllBtn = document.getElementById(selectAllButtonId);
        const deleteBtn = document.getElementById(deleteButtonId);

        if (!selectAllBtn) throw new Error(`Element with id ${selectAllButtonId} not found`);
        if (!deleteBtn) throw new Error(`Element with id ${deleteButtonId} not found`);

        this.selectAllButton = selectAllBtn;
        this.deleteButton = deleteBtn;
        this.onSelectionChange = onSelectionChange;
        this.init();
    }

    private init() {
        this.setupEventListeners();
        this.updateUI();
    }

    private setupEventListeners() {
        this.selectAllButton.addEventListener('click', () => this.toggleSelectAll());
    }


    public toggleSelection(id: number) {
        if (this.selectionState.selectedIds.has(id)) {
            this.selectionState.selectedIds.delete(id);
            this.selectionState.isAllSelected = false;
        } else {
            this.selectionState.selectedIds.add(id);
            // Check if all items are now selected
            this.selectionState.isAllSelected = this.availableIds.length > 0 && 
                this.availableIds.every(id => this.selectionState.selectedIds.has(id));
        }
        this.updateUI();
        this.onSelectionChange(this.selectionState);
    }

    public isSelected(id: number): boolean {
        return this.selectionState.selectedIds.has(id);
    }

    public getSelectedIds(): Set<number> {
        return this.selectionState.selectedIds;
    }

    public clearSelection() {
        this.selectionState = {
            selectedIds: new Set(),
            isAllSelected: false
        };
        this.updateUI();
        this.onSelectionChange(this.selectionState);
    }

    private availableIds: number[] = [];

    public setAvailableItems(ids: number[]) {
        this.availableIds = ids;
        if (this.selectionState.isAllSelected) {
            this.selectionState.selectedIds = new Set(this.availableIds);
            this.updateUI();
        }
    }

    public toggleSelectAll() {
        console.log('[DEBUG_LOG] Select all button clicked');
        this.selectionState.isAllSelected = !this.selectionState.isAllSelected;
        if (this.selectionState.isAllSelected) {
            this.selectionState.selectedIds = new Set(this.availableIds);
        } else {
            this.selectionState.selectedIds.clear();
        }
        console.log(`All selected: ${this.selectionState.isAllSelected}`);
        console.log(this.selectionState);
        this.onSelectionChange(this.selectionState);
        this.updateUI();
    }

    private updateUI() {
        console.log('[DEBUG_LOG] Updating UI');
        // Update select all button text
        this.selectAllButton.textContent = this.selectionState.isAllSelected ? 'Deselect All' : 'Select All';

        console.log(`Number of selected bookmarks: ${this.selectionState.selectedIds.size}`)

        // Show/hide delete button
        if (this.selectionState.selectedIds.size > 0) {
            this.deleteButton.classList.remove('hidden');
            this.deleteButton.textContent = `Delete Selected (${this.selectionState.selectedIds.size})`;
        } else {
            this.deleteButton.classList.add('hidden');
        }
    }
}
