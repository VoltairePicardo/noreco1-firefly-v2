import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { PclService } from '../pcl.service';

const MAX_AMOUNT = 1000;

@Component({
    selector: 'app-pcl-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS
    ],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './pcl-add-edit.component.html'
})
export class PclAddEditComponent {
    module    = 'Petty Cash Liquidation';
    subModule = 'Create';
    menuLink  = 'pcl';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    submit     = false;
    isLoading  = signal(false);
    loadingItems = false;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    /** Selected PCV */
    pettyCashTrans: any  = null;
    releasedPCVs: any[]  = [];

    /** Liquidation items: populated from PCV details */
    items: {
        remarks:    string;
        pcvAmount:  number;
        amount:     number;
        orNumber:   string;
    }[] = [];

    /** Signatories */
    approvingOfficer: any = null;
    receivingOfficer: any = null;

    readonly maxAmount = MAX_AMOUNT;

    private service      = inject(PclService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadReleasedPCVs();
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.loadDefaultSignatories();
            }
        });
    }

    loadReleasedPCVs(): void {
        this.service.getReleasedPCVs().subscribe({
            next: (data) => {
                // Filter for Released status only (status id 25 or status text "Released")
                this.releasedPCVs = (data || []).filter((pcv: any) => {
                    const s = (pcv.status || pcv.documentStatus || '').toString();
                    return s === 'Released' || s === '25';
                });
            },
            error: () => { this.releasedPCVs = []; }
        });
    }

    loadDefaultSignatories(): void {
        this.service.defaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.approvingOfficer = data.approvedBy || data.approvingOfficer || null;
                    this.receivingOfficer = data.receivedBy || data.receivingOfficer || null;
                }
            },
            error: () => {}
        });
    }

    onPCVChange(): void {
        this.items = [];
        if (this.pettyCashTrans?.id) {
            this.loadingItems = true;
            const pclId = this.editMode ? (this.id || 0) : 0;
            this.service.getPCVDetails(this.pettyCashTrans.id).subscribe({
                next: (data) => {
                    this.loadingItems = false;
                    this.items = (data || []).map((d: any) => ({
                        remarks:   d.remarks || '',
                        pcvAmount: Number(d.amount) || 0,
                        amount:    Number(d.amount) || 0,   // pre-filled with PCV detail amount
                        orNumber:  ''
                    }));
                    // If reimbursement type, signatories are auto-set from defaults (backend handles this)
                },
                error: () => { this.loadingItems = false; }
            });
        }
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.pettyCashTrans   = data.pettyCashTrans || null;
                    this.approvingOfficer = data.approvingOfficer || data.approvedBy || null;
                    this.receivingOfficer = data.receivingOfficer || data.receivedBy || null;

                    if (this.pettyCashTrans?.id) {
                        this.loadingItems = true;
                        this.service.getPCVDetails(this.pettyCashTrans.id).subscribe({
                            next: (details) => {
                                this.loadingItems = false;
                                const savedItems = data.pettyCashLiquidationDetails || [];
                                this.items = (details || []).map((d: any) => {
                                    const saved = savedItems.find((s: any) => s.remarks === d.remarks);
                                    return {
                                        remarks:   d.remarks || '',
                                        pcvAmount: Number(d.amount) || 0,
                                        amount:    Number(saved?.amount ?? d.amount) || 0,
                                        orNumber:  saved?.orNumber || ''
                                    };
                                });
                            },
                            error: () => { this.loadingItems = false; }
                        });
                    }
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

    get totalAmount(): number {
        return this.items.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
    }

    get isOverLimit(): boolean {
        return this.totalAmount > MAX_AMOUNT;
    }

    isValid(): boolean {
        if (!this.pettyCashTrans) return false;
        if (this.items.length === 0) return false;
        if (this.totalAmount <= 0) return false;
        if (this.isOverLimit) return false;
        if (!this.approvingOfficer) return false;
        if (!this.receivingOfficer) return false;
        return true;
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) return;

        this.formSubmit = true;
        const payload: any = {
            id:           this.editMode ? this.id : null,
            pettyCashTrans: { id: this.pettyCashTrans.id },
            amount:       this.totalAmount,
            approvingOfficer: this.approvingOfficer ? { accountNo: this.approvingOfficer.accountNo || this.approvingOfficer.id } : null,
            receivingOfficer: this.receivingOfficer ? { accountNo: this.receivingOfficer.accountNo || this.receivingOfficer.id } : null,
            pettyCashLiquidationDetails: this.items
                .filter(item => Number(item.amount) > 0)
                .map(item => ({
                    remarks:  item.remarks || '',
                    amount:   Number(item.amount) || 0,
                    orNumber: item.orNumber?.trim() || null
                }))
        };

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink]);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }

    cancel(): void {
        this.router.navigate(['/' + this.menuLink]);
    }

    compareFn(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    get isReimbursement(): boolean {
        return this.pettyCashTrans?.request === 'Reimbursement';
    }
}
