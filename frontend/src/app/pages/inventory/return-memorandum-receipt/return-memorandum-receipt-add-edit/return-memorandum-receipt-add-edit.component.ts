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
import { MemorandumReceipt, SlEntity } from '@/app/models/inventory-modules/memorandum-receipt.model';
import { StockWithdrawalDetail } from '@/app/models/inventory-modules/stock-withdrawal.model';
import { Office } from '@/app/models/shared/reference.model';
import {MemorandumReceiptService} from '@/app/pages/inventory/memorandum-receipt/memorandum-receipt.service';

interface ReturnDetailRow {
    stockWithdrawalDetail?: StockWithdrawalDetail;
    quantity?: number;
    returned?: number;
    returnedQuantity: number;
    usable: boolean;
    selected: boolean;
    itemDescription?: string;
}

interface ReturnMemorandumReceiptPayload {
    id?: number;
    date: string;
    employee: { accountNo: number; name?: string } | null;
    office: { id: number } | null;
    memorandumReceipt: { id: number } | null;
    remarks: string;
    returnMemorandumReceiptDetails: ReturnDetailRow[];
}

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

    id: number | null = null;
    editMode  = false;
    isLoading = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    date    = '';
    remarks = '';

    selectedEmployee = signal<SlEntity | null>(null);

    offices        = signal<Office[]>([]);
    selectedOffice = signal<Office | null>(null);

    memorandumReceipts = signal<MemorandumReceipt[]>([]);
    isLoadingMRs        = signal(false);

    selectedMR    = signal<MemorandumReceipt | null>(null);
    returnDetails = signal<ReturnDetailRow[]>([]);

    private service      = inject(ReturnMemorandumReceiptService);
    private memorandumReceiptService      = inject(MemorandumReceiptService);
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
        if (this.id == null) return;
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
                    const found = this.offices().find(o => o.id === data.office!.id);
                    this.selectedOffice.set(found ?? data.office);
                }

                if (data.memorandumReceipt?.id) {
                    this.selectedMR.set(data.memorandumReceipt);
                }

                this.returnDetails.set((data.returnMemorandumReceiptDetails || []).map((d): ReturnDetailRow => ({
                    stockWithdrawalDetail: d.stockWithdrawalDetail,
                    quantity:              d.quantity,
                    returned:              d.returnedQuantity,
                    returnedQuantity:      d.returnedQuantity ?? 0,
                    usable:                d.usable ?? false,
                    selected:              true
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
                this.selectedEmployee.set(result.data as SlEntity);
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
        this.memorandumReceiptService.getAllEmployeeMemorandumReceipt(employee.accountNo, this.editMode).subscribe({
            next: (data) => {
                const list = data || [];
                this.memorandumReceipts.set(list);
                this.isLoadingMRs.set(false);
                if (clearDetails) {
                    this.selectedMR.set(null);
                    this.returnDetails.set([]);
                }
                if (list.length > 0 && !this.selectedMR()) {
                    this.selectMR(list[0]);
                }
            },
            error: () => {
                this.isLoadingMRs.set(false);
                this.alertService.error(this.module, 'Failed to load Memorandum Receipts.', '');
            }
        });
    }

    selectMR(mr: MemorandumReceipt): void {
        this.selectedMR.set(mr);
        const details: ReturnDetailRow[] = (mr.memorandumReceiptDetails || []).map((d): ReturnDetailRow => ({
            stockWithdrawalDetail: d.stockWithdrawalDetail,
            quantity:              d.quantity,
            returned:              d.returned,
            returnedQuantity:      (d.quantity ?? 0) - (d.returned ?? 0),
            usable:                true,
            selected:              false
        }));
        this.returnDetails.set(details);
    }

    detailQuantityChanged(detail: ReturnDetailRow): void {
        const max = (detail.quantity ?? 0) - (detail.returned ?? 0);
        if (detail.returnedQuantity >= max) {
            detail.returnedQuantity = max;
        }
        this.returnDetails.update(list => [...list]);
    }

    compareById(a: { id: number } | null, b: { id: number } | null): boolean {
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

        const payload: ReturnMemorandumReceiptPayload = {
            date:     this.date,
            employee: selectedEmployee?.accountNo != null
                ? { accountNo: selectedEmployee.accountNo, name: selectedEmployee.name || selectedEmployee.fullName }
                : null,
            office: selectedOffice?.id ? { id: selectedOffice.id } : null,
            memorandumReceipt: selectedMR?.id ? { id: selectedMR.id } : null,
            remarks: this.remarks.trim(),
            returnMemorandumReceiptDetails: selectedDetails
        };

        if (this.editMode && this.id != null) payload.id = this.id;

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
