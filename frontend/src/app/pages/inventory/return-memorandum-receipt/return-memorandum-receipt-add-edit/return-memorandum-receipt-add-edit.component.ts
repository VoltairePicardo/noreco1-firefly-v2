import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
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
    templateUrl: './return-memorandum-receipt-add-edit.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReturnMemorandumReceiptAddEditComponent implements OnInit {
    module    = 'Return Memorandum Receipt';
    subModule = 'Create';
    menuLink  = 'return-memorandum-receipt';

    id: any   = null;
    editMode  = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    date    = '';
    remarks = '';

    selectedEmployee = signal<any>(null);

    offices = signal<any[]>([]);
    selectedOffice = signal<any>(null);

    memorandumReceipts = signal<any[]>([]);
    isLoadingMRs = signal(false);

    selectedMR = signal<any>(null);
    returnDetails = signal<any[]>([]);

    private service      = inject(ReturnMemorandumReceiptService);
    private modalService = inject(ModalService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
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

                if (data.employee?.accountNo) {
                    this.selectedEmployee.set(data.employee);
                    this.loadEmployeeMRs(false);
                }

                if (data.office?.id) {
                    const found = this.offices().find(o => o.id === data.office.id);
                    this.selectedOffice.set(found ?? data.office);
                }

                if (data.memorandumReceipt?.id) {
                    this.selectedMR.set(data.memorandumReceipt);
                }

                this.returnDetails.set((data.returnMemorandumReceiptDetails || []).map((d: any) => ({
                    ...d,
                    returned: d.returnedQuantity,
                    selected: true
                })));
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openEmployeeBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent, {}, { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.selectedEmployee.set(result.data);
                this.memorandumReceipts.set([]);
                this.selectedMR.set(null);
                this.returnDetails.set([]);
                this.loadEmployeeMRs(true);
            }
        } catch { }
    }

    resetEmployee(): void {
        this.selectedEmployee.set(null);
        this.memorandumReceipts.set([]);
        this.selectedMR.set(null);
        this.returnDetails.set([]);
    }

    loadEmployeeMRs(clearDetails: boolean): void {
        const employee = this.selectedEmployee();
        if (!employee?.accountNo) return;
        this.isLoadingMRs.set(true);
        this.service.getEmployeeMemorandumReceipts(employee.accountNo).subscribe({
            next: (data) => {
                this.memorandumReceipts.set(data || []);
                this.isLoadingMRs.set(false);
                if (clearDetails) {
                    this.selectedMR.set(null);
                    this.returnDetails.set([]);
                }
            },
            error: () => {
                this.isLoadingMRs.set(false);
                this.alertService.error(this.module, 'Failed to load Memorandum Receipts.', '');
            }
        });
    }

    selectMR(mr: any): void {
        this.selectedMR.set(mr);
        const details: any[] = [];

        if (mr?.memorandumReceiptDetails?.length > 0) {
            for (const d of mr.memorandumReceiptDetails) {
                details.push({
                    ...d,
                    returnedQuantity: d.quantity - d.returned,
                    usable:           true,
                    selected:         false
                });
            }
        }
        this.returnDetails.set(details);
    }

    detailQuantityChanged(detail: any): void {
        const max = detail.quantity - detail.returned;
        if (detail.returnedQuantity >= max) {
            detail.returnedQuantity = max;
        }
        this.returnDetails.update(list => [...list]);
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    save(): void {
        const returnDetails = this.returnDetails();
        if (returnDetails.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please select a Memorandum Receipt.');
            return;
        }

        const selectedDetails = returnDetails.filter(d => d.selected);
        if (selectedDetails.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please select at least one item.');
            return;
        }

        if (!this.remarks?.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Please enter remarks.');
            return;
        }

        this.isLoading.set(true);

        const selectedEmployee = this.selectedEmployee();
        const selectedOffice   = this.selectedOffice();
        const selectedMR       = this.selectedMR();

        const payload: any = {
            date:     this.date,
            employee: selectedEmployee
                ? { accountNo: selectedEmployee.accountNo, name: selectedEmployee.name || selectedEmployee.fullName }
                : null,
            office: selectedOffice?.id ? { id: selectedOffice.id } : null,
            memorandumReceipt: selectedMR?.id ? { id: selectedMR.id } : null,
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
