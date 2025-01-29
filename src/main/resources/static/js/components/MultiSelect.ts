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

    public toggleSelectAll() {
        this.selectionState.isAllSelected = !this.selectionState.isAllSelected;
        this.onSelectionChange(this.selectionState);
        this.updateUI();
    }

    public toggleSelection(id: number) {
        if (this.selectionState.selectedIds.has(id)) {
            this.selectionState.selectedIds.delete(id);
        } else {
            this.selectionState.selectedIds.add(id);
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

    public setTotalItems(totalItems: number) {
        if (this.selectionState.isAllSelected) {
            this.selectionState.selectedIds = new Set(Array.from({ length: totalItems }, (_, i) => i));
        }
        this.updateUI();
    }

    private updateUI() {
        // Update select all button text
        this.selectAllButton.textContent = this.selectionState.isAllSelected ? 'Deselect All' : 'Select All';

        // Show/hide delete button
        if (this.selectionState.selectedIds.size > 0) {
            this.deleteButton.classList.remove('hidden');
            this.deleteButton.textContent = `Delete Selected (${this.selectionState.selectedIds.size})`;
        } else {
            this.deleteButton.classList.add('hidden');
        }
    }
}