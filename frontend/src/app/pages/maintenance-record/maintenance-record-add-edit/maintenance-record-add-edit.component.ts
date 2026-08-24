import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { MaintenanceRecordService } from '../maintenance-record.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseAssetModalComponent } from '@/app/shared/modals/browse-asset-modal/browse-asset-modal.component';
import { BrowseVoucherModalComponent } from '@/app/shared/modals/browse-voucher-modal/browse-voucher-modal.component';
import { BrowseStockReleaseModalComponent } from '@/app/shared/modals/browse-stock-release-modal/browse-stock-release-modal.component';

@Component({
    selector: 'app-maintenance-record-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './maintenance-record-add-edit.component.html'
})
export class MaintenanceRecordAddEditComponent {
    module    = 'Maintenance Record';
    subModule = 'Create';
    menuLink  = 'maintenance-record';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Form fields
    maintenanceDate  = '';
    nextPmsDate      = '';
    odometerReading: number | null = null;
    code             = '';

    // Asset (browse)
    asset: any = null;

    // Voucher transaction (browse)
    voucherTransaction: any = null;

    // Dynamic line items
    workItems:  { description: string; amount: number }[] = [];
    otherItems: { description: string; amount: number }[] = [];

    // Stock releases
    stockReleases: {
        stockRelease: { id: number };
        code: string;
        description: string;
        voucherDate: string;
        items: { itemStock?: any; description?: string; totalCost: number; selected: boolean }[];
    }[] = [];

    private service      = inject(MaintenanceRecordService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.addWorkItem();
                this.addOtherItem();
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.code            = data.code || '';
                    this.maintenanceDate = data.maintenanceDate ? new Date(data.maintenanceDate).toISOString().substring(0, 10) : '';
                    this.nextPmsDate     = data.nextPmsDate     ? new Date(data.nextPmsDate).toISOString().substring(0, 10)     : '';
                    this.odometerReading = data.odometerReading ?? null;
                    this.asset           = data.asset           || null;
                    this.voucherTransaction = data.voucherTransaction || null;

                    this.workItems = (data.maintenanceRecordWorks || []).map((w: any) => ({
                        description: w.description || '',
                        amount:      Number(w.amount) || 0
                    }));
                    if (this.workItems.length === 0) this.addWorkItem();

                    this.otherItems = (data.maintenanceRecordOtherItems || []).map((o: any) => ({
                        description: o.description || '',
                        amount:      Number(o.amount) || 0
                    }));
                    if (this.otherItems.length === 0) this.addOtherItem();

                    this.stockReleases = (data.maintenanceRecordMaterialReleases || []).map((sr: any) => ({
                        stockRelease: { id: sr.stockRelease?.id || sr.id },
                        code:         sr.code         || sr.stockRelease?.code        || '',
                        description:  sr.description  || sr.stockRelease?.description || '',
                        voucherDate:  sr.voucherDate  || sr.stockRelease?.voucherDate || '',
                        items: (sr.items || []).map((item: any) => ({
                            ...item,
                            selected: !(data.excludedStockReleaseItems || []).some((ex: any) => ex.id === item.id)
                        }))
                    }));
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

    // ── Work items ──────────────────────────────────────────────────────────────

    addWorkItem(): void { this.workItems.push({ description: '', amount: 0 }); }
    removeWorkItem(i: number): void { this.workItems.splice(i, 1); }
    get totalWorkAmount(): number { return this.workItems.reduce((s, w) => s + (Number(w.amount) || 0), 0); }

    // ── Other items ─────────────────────────────────────────────────────────────

    addOtherItem(): void { this.otherItems.push({ description: '', amount: 0 }); }
    removeOtherItem(i: number): void { this.otherItems.splice(i, 1); }
    get totalOtherAmount(): number { return this.otherItems.reduce((s, o) => s + (Number(o.amount) || 0), 0); }

    // ── Stock releases ──────────────────────────────────────────────────────────

    async browseStockRelease(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseStockReleaseModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                const sr = result.data;
                const alreadyAdded = this.stockReleases.some(s => s.stockRelease.id === sr.id);
                if (alreadyAdded) {
                    this.alertService.warning(this.module, 'Duplicate', 'This stock release has already been added.');
                    return;
                }
                this.stockReleases.push({
                    stockRelease: { id: sr.id },
                    code:         sr.code        || '',
                    description:  sr.description || '',
                    voucherDate:  sr.voucherDate  || '',
                    items: (sr.items || []).map((item: any) => ({ ...item, selected: true }))
                });
            }
        } catch { }
    }

    removeStockRelease(i: number): void { this.stockReleases.splice(i, 1); }

    // ── Asset browse ────────────────────────────────────────────────────────────

    async browseAsset(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseAssetModalComponent, {}, { size: 'xl', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.asset = result.data;
                // Clear voucher when asset changes
                this.voucherTransaction = null;
            }
        } catch { }
    }

    clearAsset(): void {
        this.asset = null;
        this.voucherTransaction = null;
    }

    // ── Voucher browse ──────────────────────────────────────────────────────────

    async browseVoucher(): Promise<void> {
        if (!this.asset) return;
        try {
            const result = await this.modalService.openModal(
                BrowseVoucherModalComponent,
                { accountNo: this.asset.accountNo || '' },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.voucherTransaction = result.data;
            }
        } catch { }
    }

    clearVoucher(): void { this.voucherTransaction = null; }

    // ── Save ─────────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.maintenanceDate) {
            this.alertService.warning(this.module, 'Validation', 'Maintenance Date is required.');
            return;
        }

        this.isLoading.set(true);

        const excludedStockReleaseItems: any[] = [];
        this.stockReleases.forEach(sr => {
            (sr.items || []).forEach((item: any) => {
                if (!item.selected && item.id) {
                    excludedStockReleaseItems.push({ id: item.id });
                }
            });
        });

        const payload: any = {
            maintenanceDate:  this.maintenanceDate,
            odometerReading:  this.odometerReading ?? null,
            nextPmsDate:      this.nextPmsDate     || null,
            asset:            this.asset ? { id: this.asset.id } : null,
            voucherTransaction: this.voucherTransaction ? { id: this.voucherTransaction.transactionId || this.voucherTransaction.id } : null,
            maintenanceRecordWorks:            this.workItems.filter(w => w.description?.trim()),
            maintenanceRecordOtherItems:       this.otherItems.filter(o => o.description?.trim()),
            maintenanceRecordMaterialReleases: this.stockReleases.map(sr => ({ stockRelease: sr.stockRelease })),
            excludedStockReleaseItems
        };

        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode ? this.service.update(payload) : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(this.module, this.editMode ? 'Updated' : 'Created', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Save', '');
            }
        });
    }
}
