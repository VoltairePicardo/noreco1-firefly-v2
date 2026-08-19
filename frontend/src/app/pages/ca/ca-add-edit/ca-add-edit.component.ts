import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { CaService } from '../ca.service';
import { SharedModalService } from '@/app/shared/modals/shared-modal-service/shared-modal.service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseBudgetLineItemModalComponent } from '@/app/shared/modals/browse-budget-line-item-modal/browse-budget-line-item-modal.component';
import { forkJoin } from 'rxjs';

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
        FlatpickrModule
    ],
    providers: [FlatpickrDefaults],
    templateUrl: './ca-add-edit.component.html'
})
export class CaAddEditComponent {
    module    = 'Cash Advance';
    subModule = 'Create';
    menuLink  = 'ca';

    id: any    = null;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);

    units           = signal<any[]>([]);
    offices         = signal<any[]>([]);
    budgetLineItems = signal<any[]>([]);
    unliquidatedCAs = signal<any[]>([]);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    particulars: CaParticular[] = [];
    signatoryNames: { [key: string]: string } = {};
    selectedBudgetLineItemText = '';

    validationForm!: UntypedFormGroup;

    private service      = inject(CaService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(SharedModalService);
    private alertService = inject(AlertService);
    private fb           = inject(FormBuilder);

    ngOnInit(): void {
        forkJoin({
            units:           this.service.getUnits(),
            budgetLineItems: this.service.getBudgetLineItems(),
            offices:         this.service.getOffices(),
        }).subscribe({
            next: (res) => {
                this.units.set(res.units || []);
                this.budgetLineItems.set(res.budgetLineItems || []);
                this.offices.set(res.offices || []);
            },
            error: () => {}
        });

        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.initForm();
                this.addParticular();
                this.loadDefaultSignatories();
                this.loadUserOffice();
                this.loadUnliquidatedCAs();
            }
        });
    }

    private today(): string {
        return new Date().toISOString().substring(0, 10);
    }

    toDateInput(val: any): string {
        if (!val) return '';
        return new Date(val).toISOString().substring(0, 10);
    }

    initForm(data?: any): void {
        const t = this.today();
        this.validationForm = this.fb.group({
            voucherDate:               [data?.voucherDate       ? this.toDateInput(data.voucherDate)       : t, Validators.required],
            periodCoveredFrom:         [data?.periodCoveredFrom ? this.toDateInput(data.periodCoveredFrom) : t],
            periodCoveredTo:           [data?.periodCoveredTo   ? this.toDateInput(data.periodCoveredTo)   : t],
            purpose:                   [data?.purpose    || '', Validators.required],
            remarks:                   [data?.remarks    || ''],
            location:                  [data?.location   || ''],
            officeId:                  [data?.office?.id || null],
            budgetLineItemDetailId:    [data?.budgetLineItemDetail?.id || null],
            recommendedByAccountNo:    [data?.recommendedBy?.accountNo    || null],
            budgetOfficerAccountNo:    [data?.budgetOfficer?.accountNo    || null],
            approvingOfficerAccountNo: [data?.approvingOfficer?.accountNo || null, Validators.required],
        });

        if (data) {
            if (data.recommendedBy?.name    || data.recommendedBy?.fullName)    this.signatoryNames['recommendedByAccountNo']    = data.recommendedBy.name    || data.recommendedBy.fullName;
            if (data.budgetOfficer?.name    || data.budgetOfficer?.fullName)    this.signatoryNames['budgetOfficerAccountNo']    = data.budgetOfficer.name    || data.budgetOfficer.fullName;
            if (data.approvingOfficer?.name || data.approvingOfficer?.fullName) this.signatoryNames['approvingOfficerAccountNo'] = data.approvingOfficer.name || data.approvingOfficer.fullName;
            if (data.budgetLineItemDetail) {
                this.selectedBudgetLineItemText = data.budgetLineItemDetail.code + ' — ' + data.budgetLineItemDetail.title;
            }
        }
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    loadUnliquidatedCAs(): void {
        this.service.getUnliquidatedList().subscribe({
            next: (data) => this.unliquidatedCAs.set(data || []),
            error: () => {}
        });
    }

    loadUserOffice(): void {
        this.service.getUserOffice().subscribe({
            next: (data) => { if (data?.id) this.form.get('officeId')?.setValue(data.id); },
            error: () => {}
        });
    }

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (!data) return;
                const patch: any = {};
                const rec  = data.recommendedBy;
                const bud  = data.budgetOfficer;
                const app  = data.approvingOfficer || data.approvedBy;
                if (rec?.accountNo)  { patch['recommendedByAccountNo']    = rec.accountNo;  this.signatoryNames['recommendedByAccountNo']    = rec.name; }
                if (bud?.accountNo)  { patch['budgetOfficerAccountNo']    = bud.accountNo;  this.signatoryNames['budgetOfficerAccountNo']    = bud.name; }
                if (app?.accountNo)  { patch['approvingOfficerAccountNo'] = app.accountNo;  this.signatoryNames['approvingOfficerAccountNo'] = app.name; }
                this.form.patchValue(patch);
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
                    this.initForm(data);
                    this.particulars = (data.cashAdvanceParticulars || []).map((p: any) => ({
                        particular: p.particular || '',
                        date:       p.date ? this.toDateInput(p.date) : '',
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
                this.form.get('budgetLineItemDetailId')?.setValue(result.data.id);
                this.selectedBudgetLineItemText = result.data.code + ' — ' + result.data.title;
            }
        } catch { }
    }

    clearBudgetLineItem(): void {
        this.form.get('budgetLineItemDetailId')?.setValue(null);
        this.selectedBudgetLineItemText = '';
    }

    async openSignatoryBrowse(field: 'recommendedByAccountNo' | 'budgetOfficerAccountNo' | 'approvingOfficerAccountNo'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.form.get(field)?.setValue(result.data.accountNo);
                this.signatoryNames[field] = result.data.name;
            }
        } catch { }
    }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }
        if (this.totalAmount <= 0) {
            this.alertService.error(this.module, 'Validation', 'Please add at least one item with an amount.');
            this.formSubmit = false;
            return;
        }

        const v = this.form.getRawValue();
        const payload: any = {
            id:               this.editMode ? this.id : null,
            voucherDate:      v.voucherDate,
            cashAdvanceDate:  v.voucherDate,
            purpose:          v.purpose.trim(),
            remarks:          v.remarks?.trim()  || null,
            location:         v.location?.trim() || null,
            periodCoveredFrom: v.periodCoveredFrom || null,
            periodCoveredTo:   v.periodCoveredTo   || null,
            amount:           this.totalAmount,
            employee:         null,
            office:               v.officeId              ? { id: v.officeId }              : null,
            budgetLineItemDetail: v.budgetLineItemDetailId ? { id: v.budgetLineItemDetailId } : null,
            recommendedBy:    v.recommendedByAccountNo    ? { accountNo: v.recommendedByAccountNo }    : null,
            budgetOfficer:    v.budgetOfficerAccountNo    ? { accountNo: v.budgetOfficerAccountNo }    : null,
            approvingOfficer: v.approvingOfficerAccountNo ? { accountNo: v.approvingOfficerAccountNo } : null,
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
                    this.router.navigate(['/' + this.menuLink, res.modelId || this.id, 'detail']);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }
}
