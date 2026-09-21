import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { CaLiquidationService } from '../ca-liquidation.service';
import { SharedModalService } from '@/app/shared/modals/shared-modal-service/shared-modal.service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import Swal from 'sweetalert2';

interface CalItem {
    cashAdvanceParticularId: number;
    particular: string;
    date: string;
    quantity: number;
    unit: any;
    originalAmount: number;
    total: number;
    amount: number;
    orNumber: string;
}

@Component({
    selector: 'app-ca-liquidation-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './ca-liquidation-add-edit.component.html'
})
export class CaLiquidationAddEditComponent {
    module    = 'CA Liquidation';
    subModule = 'Create';
    menuLink  = 'ca-liquidation';

    @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    submit     = false;
    isLoading  = signal(false);
    loadingItems = false;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    offices = signal<any[]>([]);

    officeId    = null as any;
    voucherDate = '';
    remarks     = '';

    cashAdvance: any    = null;
    cashAdvances: any[] = [];

    recommendedByAccountNo: any = null;
    approvingOfficerAccountNo: any = null;
    signatoryNames: { [key: string]: string } = {};

    items: CalItem[] = [];
    origNumberOfItems = 0;

    attachments: { file: File; name: string }[] = [];
    filesToRemove: any[] = [];
    transactionId: any = null;

    private service      = inject(CaLiquidationService);
    private route         = inject(ActivatedRoute);
    private router        = inject(Router);
    private modalService  = inject(SharedModalService);
    private alertService  = inject(AlertService);

    private today(): string {
        return new Date().toISOString().substring(0, 10);
    }

    toDateInput(val: any): string {
        if (!val) return '';
        return new Date(val).toISOString().substring(0, 10);
    }

    ngOnInit(): void {
        this.loadCashAdvances();
        this.loadOffices();

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.voucherDate = this.today();
                this.loadDefaultSignatories();
                this.loadUserOffice();
            }
        });
    }

    loadOffices(): void {
        this.service.getOffices().subscribe({
            next: (data) => this.offices.set(data || []),
            error: () => {}
        });
    }

    loadUserOffice(): void {
        this.service.getUserOffice().subscribe({
            next: (data) => { if (data?.id) this.officeId = data.id; },
            error: () => {}
        });
    }

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (!data) return;
                const rec = data.recommendedBy;
                const app = data.approvingOfficer || data.approvedBy;
                if (rec?.accountNo) { this.recommendedByAccountNo    = rec.accountNo; this.signatoryNames['recommendedByAccountNo']    = rec.name; }
                if (app?.accountNo) { this.approvingOfficerAccountNo = app.accountNo; this.signatoryNames['approvingOfficerAccountNo'] = app.name; }
            },
            error: () => {}
        });
    }

    loadCashAdvances(): void {
        this.service.getCashAdvanceList().subscribe({
            next: (data) => { this.cashAdvances = (data || []).filter((ca: any) => !ca.isLiquidated); },
            error: () => { this.cashAdvances = []; }
        });
    }

    private mapParticular(p: any): CalItem {
        return {
            cashAdvanceParticularId: p.id,
            particular:      p.particular || '',
            date:            p.date ? this.toDateInput(p.date) : '',
            quantity:        Number(p.quantity) || 0,
            unit:            p.unit || null,
            originalAmount:  Number(p.amount) || 0,
            total:           Number(p.total) || 0,
            amount:          0,
            orNumber:        ''
        };
    }

    onCashAdvanceChange(): void {
        if (!this.cashAdvance?.id) {
            this.items = [];
            this.origNumberOfItems = 0;
            return;
        }
        this.fetchItems();
    }

    refreshItems(): void {
        if (!this.cashAdvance?.id) return;
        this.fetchItems();
    }

    private fetchItems(): void {
        this.loadingItems = true;
        const calId = this.editMode ? (this.id || 0) : 0;
        this.service.getParticularsForLiquidation(this.cashAdvance.id, calId).subscribe({
            next: (data) => {
                this.loadingItems = false;
                this.items = (data || []).map((p: any) => this.mapParticular(p));
                this.origNumberOfItems = this.items.length;
            },
            error: () => { this.loadingItems = false; }
        });
    }

    async clearCashAdvance(): Promise<void> {
        if (this.editMode && this.cashAdvance) {
            const result = await Swal.fire({
                title: 'Confirm',
                text: 'Removing Cash Advance will clear the liquidation items. Do you want to continue?',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Yes',
                cancelButtonText: 'Cancel'
            });
            if (!result.isConfirmed) return;
        }
        this.cashAdvance       = null;
        this.items             = [];
        this.origNumberOfItems = 0;
    }

    removeItem(index: number): void {
        this.items.splice(index, 1);
    }

    async openSignatoryBrowse(field: 'recommendedByAccountNo' | 'approvingOfficerAccountNo'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                if (field === 'recommendedByAccountNo') this.recommendedByAccountNo = result.data.accountNo;
                else this.approvingOfficerAccountNo = result.data.accountNo;
                this.signatoryNames[field] = result.data.name;
            }
        } catch { }
    }

    openFilePicker(): void {
        this.fileInputRef?.nativeElement.click();
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files?.length) return;

        const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
        for (const file of Array.from(input.files)) {
            if (!allowed.includes(file.type)) {
                this.alertService.warning(this.module, 'Invalid file type', `${file.name} must be JPG, PNG, or PDF.`);
                continue;
            }
            this.attachments.push({ file, name: file.name });
        }
        input.value = '';
    }

    removeAttachment(index: number): void {
        this.attachments.splice(index, 1);
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate    = data.voucherDate ? this.toDateInput(data.voucherDate) : '';
                    this.remarks        = data.remarks || '';
                    this.officeId       = data.office?.id || null;
                    this.transactionId  = data.transaction?.id || null;
                    this.cashAdvance    = data.cashAdvance || null;

                    if (data.recommendedBy?.accountNo)    { this.recommendedByAccountNo    = data.recommendedBy.accountNo;    this.signatoryNames['recommendedByAccountNo']    = data.recommendedBy.name    || data.recommendedBy.fullName; }
                    if (data.approvingOfficer?.accountNo) { this.approvingOfficerAccountNo = data.approvingOfficer.accountNo; this.signatoryNames['approvingOfficerAccountNo'] = data.approvingOfficer.name || data.approvingOfficer.fullName; }

                    this.items = (data.cashAdvanceLiquidationItems || []).map((item: any) => ({
                        cashAdvanceParticularId: item.cashAdvanceParticular?.id,
                        particular:     item.cashAdvanceParticular?.particular || '',
                        date:           item.cashAdvanceParticular?.date ? this.toDateInput(item.cashAdvanceParticular.date) : '',
                        quantity:       Number(item.cashAdvanceParticular?.quantity) || 0,
                        unit:           item.cashAdvanceParticular?.unit || null,
                        originalAmount: Number(item.cashAdvanceParticular?.amount) || 0,
                        total:          Number(item.cashAdvanceParticular?.total) || 0,
                        amount:         Number(item.amount) || 0,
                        orNumber:       item.orNumber || ''
                    }));
                    this.origNumberOfItems = this.items.length;
                } else {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    get totalOriginalAmount(): number {
        return this.items.reduce((sum, item) => sum + (Number(item.originalAmount) || 0), 0);
    }

    get totalRowAmount(): number {
        return this.items.reduce((sum, item) => sum + (Number(item.total) || 0), 0);
    }

    get totalAmount(): number {
        return this.items.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
    }

    isValid(): boolean {
        return !!(this.voucherDate && this.cashAdvance && this.approvingOfficerAccountNo &&
            this.recommendedByAccountNo && this.totalAmount > 0);
    }

    compareFn(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) return;

        this.formSubmit = true;
        const payload: any = {
            id:               this.editMode ? this.id : null,
            voucherDate:      this.voucherDate,
            amount:           this.totalAmount,
            office:           this.officeId ? { id: this.officeId } : null,
            cashAdvance:      this.cashAdvance?.id ? { id: this.cashAdvance.id } : null,
            approvingOfficer: this.approvingOfficerAccountNo ? { accountNo: this.approvingOfficerAccountNo } : null,
            recommendedBy:    this.recommendedByAccountNo    ? { accountNo: this.recommendedByAccountNo }    : null,
            remarks:          this.remarks?.trim() || null,
            forClearing:      this.origNumberOfItems === this.items.length,
            cashAdvanceLiquidationItems: this.items.map(item => ({
                cashAdvanceParticular: { id: item.cashAdvanceParticularId },
                orNumber: item.orNumber?.trim() || null,
                amount:   Number(item.amount) || 0
            }))
        };

        const files = this.attachments.map(a => a.file);
        const req$ = this.editMode
            ? this.service.update(payload, files, this.filesToRemove)
            : this.service.create(payload, files);

        req$.subscribe({
            next: (res: any) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId || this.id, 'detail']);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }
}
