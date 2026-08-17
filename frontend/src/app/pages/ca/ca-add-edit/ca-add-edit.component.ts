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
import { CaService } from '../ca.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseBudgetLineItemModalComponent } from '@/app/shared/modals/browse-budget-line-item-modal/browse-budget-line-item-modal.component';

interface CaParticular {
    particular: string;
    date:       string;
    quantity:   number;
    unit:       any;
    amount:     number;
}

@Component({
    selector: 'app-ca-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './ca-add-edit.component.html'
})
export class CaAddEditComponent {
    module    = 'Cash Advance';
    subModule = 'Create';
    menuLink  = 'ca';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    submit     = false;
    isLoading  = signal(false);

    units              = signal<any[]>([]);
    offices            = signal<any[]>([]);
    budgetLineItems    = signal<any[]>([]);
    unliquidatedCAs    = signal<any[]>([]);
    selectedOffice:        any = null;
    budgetLineItemDetail:  any = null;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate       = '';
    purpose           = '';
    remarks           = '';
    location          = '';
    periodCoveredFrom = '';
    periodCoveredTo   = '';

    recommendedBy:    any = null;
    budgetOfficer:    any = null;
    approvingOfficer: any = null;

    particulars: CaParticular[] = [];

    private service      = inject(CaService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadUnits();
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadOffices(() => this.loadForEdit());
            } else {
                this.subModule = 'Create';
                this.addParticular();
                this.setDefaultDates();
                this.loadDefaultSignatories();
                this.loadOffices(() => this.loadUserOffice());
                this.loadUnliquidatedCAs();
            }
        });
    }

    private today(): string {
        return new Date().toISOString().substring(0, 10);
    }

    setDefaultDates(): void {
        const t = this.today();
        this.voucherDate       = t;
        this.periodCoveredFrom = t;
        this.periodCoveredTo   = t;
    }

    loadUnits(): void {
        this.service.getUnits().subscribe({
            next: (data) => { this.units.set(data || []); this.loadBudgetLineItems(); },
            error: () => {}
        });
    }

    loadUnliquidatedCAs(): void {
        this.service.getUnliquidatedList().subscribe({
            next: (data) => this.unliquidatedCAs.set(data || []),
            error: () => {}
        });
    }

    loadBudgetLineItems(): void {
        this.service.getBudgetLineItems().subscribe({
            next: (data) => this.budgetLineItems.set(data || []),
            error: () => {}
        });
    }

    loadOffices(callback?: () => void): void {
        this.service.getOffices().subscribe({
            next: (data) => { this.offices.set(data || []); if (callback) callback(); },
            error: () => { if (callback) callback(); }
        });
    }

    loadUserOffice(): void {
        this.service.getUserOffice().subscribe({
            next: (data) => { this.selectedOffice = data || null; },
            error: () => {}
        });
    }

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.recommendedBy    = data.recommendedBy    || null;
                    this.budgetOfficer    = data.budgetOfficer    || null;
                    this.approvingOfficer = data.approvingOfficer || data.approvedBy || null;
                }
            },
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate       = data.voucherDate       ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                    this.periodCoveredFrom = data.periodCoveredFrom ? new Date(data.periodCoveredFrom).toISOString().substring(0, 10) : '';
                    this.periodCoveredTo   = data.periodCoveredTo   ? new Date(data.periodCoveredTo).toISOString().substring(0, 10) : '';
                    this.purpose           = data.purpose    || '';
                    this.remarks           = data.remarks    || '';
                    this.location          = data.location   || '';
                    this.selectedOffice       = data.office              || null;
                    this.budgetLineItemDetail = data.budgetLineItemDetail || null;
                    this.recommendedBy        = data.recommendedBy       || null;
                    this.budgetOfficer     = data.budgetOfficer    || null;
                    this.approvingOfficer  = data.approvingOfficer || null;
                    this.particulars = (data.cashAdvanceParticulars || []).map((p: any) => ({
                        particular: p.particular || '',
                        date:       p.date ? new Date(p.date).toISOString().substring(0, 10) : '',
                        quantity:   Number(p.quantity) || 1,
                        unit:       p.unit || null,
                        amount:     Number(p.amount) || 0
                    }));
                    if (this.particulars.length === 0) this.addParticular();
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

    addParticular(): void {
        this.particulars.push({ particular: '', date: '', quantity: 1, unit: null, amount: 0 });
    }

    removeParticular(index: number): void {
        if (this.particulars.length > 1) this.particulars.splice(index, 1);
    }

    rowTotal(p: CaParticular): number {
        return (Number(p.quantity) || 0) * (Number(p.amount) || 0);
    }

    get totalAmount(): number {
        return this.particulars.reduce((sum, p) => sum + this.rowTotal(p), 0);
    }

    async openBudgetLineItemBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseBudgetLineItemModalComponent,
                { items: this.budgetLineItems() },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.budgetLineItemDetail = result.data;
            }
        } catch { }
    }

    clearBudgetLineItem(): void {
        this.budgetLineItemDetail = null;
    }

    async openSignatoryBrowse(field: 'recommendedBy' | 'budgetOfficer' | 'approvingOfficer'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    clearSignatory(field: 'recommendedBy' | 'budgetOfficer' | 'approvingOfficer'): void {
        this[field] = null;
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    isValid(): boolean {
        return !!(
            this.voucherDate &&
            this.purpose?.trim() &&
            this.recommendedBy &&
            this.approvingOfficer &&
            this.totalAmount > 0
        );
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) return;

        this.formSubmit = true;
        const payload: any = {
            id:               this.editMode ? this.id : null,
            voucherDate:      this.voucherDate,
            cashAdvanceDate:  this.voucherDate,
            purpose:          this.purpose.trim(),
            remarks:          this.remarks?.trim() || null,
            location:         this.location?.trim() || null,
            periodCoveredFrom: this.periodCoveredFrom || null,
            periodCoveredTo:   this.periodCoveredTo   || null,
            amount:           this.totalAmount,
            employee:         null,
            office:               this.selectedOffice       ? { id: this.selectedOffice.id }       : null,
            budgetLineItemDetail: this.budgetLineItemDetail ? { id: this.budgetLineItemDetail.id } : null,
            recommendedBy:    this.recommendedBy    ? { id: this.recommendedBy.id }    : null,
            budgetOfficer:    this.budgetOfficer    ? { id: this.budgetOfficer.id }    : null,
            approvingOfficer: this.approvingOfficer ? { id: this.approvingOfficer.id } : null,
            cashAdvanceParticulars: this.particulars
                .filter(p => p.particular?.trim())
                .map(p => ({
                    particular: p.particular.trim(),
                    date:       p.date || null,
                    quantity:   Number(p.quantity) || 1,
                    unit:       p.unit ? { id: p.unit.id } : null,
                    amount:     Number(p.amount) || 0
                }))
        };

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    const id = res.modelId || this.id;
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }
}
