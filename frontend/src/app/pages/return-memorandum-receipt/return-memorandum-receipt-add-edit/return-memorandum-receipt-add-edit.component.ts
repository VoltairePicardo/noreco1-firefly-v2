import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { ReturnMemorandumReceiptService } from '../return-memorandum-receipt.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerX, tablerArrowLeft, tablerCheck } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-return-memorandum-receipt-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        RouterLink
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerX, tablerArrowLeft, tablerCheck })
    ],
    templateUrl: './return-memorandum-receipt-add-edit.component.html'
})
export class ReturnMemorandumReceiptAddEditComponent {
    module    = 'Return Memorandum Receipt';
    subModule = 'Create';
    menuLink  = 'return-memorandum-receipt';

    id: any   = null;
    editMode  = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    date    = '';
    remarks = '';

    // Employee
    selectedEmployee: any = null;

    // Offices
    offices = signal<any[]>([]);
    selectedOffice: any = null;

    // Memorandum Receipts list (loaded after employee selection)
    memorandumReceipts: any[] = [];
    isLoadingMRs = false;

    // Selected MR and its details
    selectedMR: any = null;
    returnDetails: any[] = [];

    private service      = inject(ReturnMemorandumReceiptService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        // Load offices
        this.service.getOffices().subscribe({
            next: (data) => this.offices.set(data || []),
            error: () => {}
        });

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.date = new Date().toISOString().substring(0, 10);
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (!data?.id) {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                    return;
                }

                this.date    = data.date ? new Date(data.date).toISOString().substring(0, 10) : '';
                this.remarks = data.remarks || '';

                // Restore employee
                if (data.employee?.accountNo) {
                    this.selectedEmployee = data.employee;
                    this.loadEmployeeMRs(false);
                }

                // Restore office
                if (data.office?.id) {
                    const found = this.offices().find(o => o.id === data.office.id);
                    this.selectedOffice = found ?? data.office;
                }

                // Restore selected MR reference
                if (data.memorandumReceipt?.id) {
                    this.selectedMR = data.memorandumReceipt;
                }

                // Restore details — mark all as selected
                this.returnDetails = (data.returnMemorandumReceiptDetails || []).map((d: any) => ({
                    ...d,
                    returned: d.returnedQuantity,
                    selected: true
                }));
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ─── Employee Browse ──────────────────────────────────────────────────────

    async openEmployeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedEmployee = result.data;
                this.memorandumReceipts = [];
                this.selectedMR       = null;
                this.returnDetails    = [];
                this.loadEmployeeMRs(true);
            }
        } catch { }
    }

    resetEmployee(): void {
        this.selectedEmployee  = null;
        this.memorandumReceipts = [];
        this.selectedMR        = null;
        this.returnDetails     = [];
    }

    loadEmployeeMRs(clearDetails: boolean): void {
        if (!this.selectedEmployee?.accountNo) return;
        this.isLoadingMRs = true;
        this.service.getEmployeeMemorandumReceipts(this.selectedEmployee.accountNo).subscribe({
            next: (data) => {
                this.memorandumReceipts = data || [];
                this.isLoadingMRs = false;
                if (clearDetails) {
                    this.selectedMR    = null;
                    this.returnDetails = [];
                }
            },
            error: () => {
                this.isLoadingMRs = false;
                this.alertService.error(this.module, 'Failed to load Memorandum Receipts.', '');
            }
        });
    }

    // ─── MR Selection ─────────────────────────────────────────────────────────

    /**
     * Called when user clicks "Return Item" on an MR row.
     * Mirrors OLD returnMemo(): populates returnDetails from MR's details.
     */
    selectMR(mr: any): void {
        this.selectedMR    = mr;
        this.returnDetails = [];

        if (mr?.memorandumReceiptDetails?.length > 0) {
            for (const d of mr.memorandumReceiptDetails) {
                this.returnDetails.push({
                    ...d,
                    returnedQuantity: d.quantity - d.returned,
                    usable:           true,
                    selected:         false
                });
            }
        }
    }

    /**
     * Constrain returnedQuantity to max: quantity − returned.
     * Mirrors OLD memorandumReceiptDetailQuantityChanged().
     */
    detailQuantityChanged(detail: any): void {
        const max = detail.quantity - detail.returned;
        if (detail.returnedQuantity >= max) {
            detail.returnedQuantity = max;
        }
    }

    // ─── compareById for office select ───────────────────────────────────────

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    // ─── Validation & Save ────────────────────────────────────────────────────

    save(): void {
        // Validation: must have selected a memorandum receipt
        if (this.returnDetails.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Memorandum Receipt.');
            return;
        }

        // Validation: at least one item must be checked
        const selectedDetails = this.returnDetails.filter(d => d.selected);
        if (selectedDetails.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please select at least one item.');
            return;
        }

        // Validation: remarks required
        if (!this.remarks?.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Please enter remarks.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            date:     this.date,
            employee: this.selectedEmployee
                ? { accountNo: this.selectedEmployee.accountNo, name: this.selectedEmployee.name || this.selectedEmployee.fullName }
                : null,
            office: this.selectedOffice?.id ? { id: this.selectedOffice.id } : null,
            memorandumReceipt: this.selectedMR?.id ? { id: this.selectedMR.id } : null,
            remarks: this.remarks.trim(),
            returnMemorandumReceiptDetails: selectedDetails
        };

        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode
            ? this.service.update(payload)
            : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(
                        this.module,
                        this.editMode ? 'Updated successfully.' : 'Created successfully.',
                        ''
                    );
                    const id = data?.modelId ?? data?.id ?? this.id;
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Save', 'An error occurred.');
            }
        });
    }
}
