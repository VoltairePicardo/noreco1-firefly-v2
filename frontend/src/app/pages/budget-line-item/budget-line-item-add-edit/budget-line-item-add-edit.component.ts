import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { BudgetLineItemService } from '../budget-line-item.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { BrowseItemModalComponent } from '@/app/shared/modals/browse-item-modal/browse-item-modal.component';

interface BliDetail {
    id:                  any;
    title:               string;
    projectType:         any;
    location:            string;
    length:              number;
    strategicInitiative: any;
    item:                any;
    specification:       string;
    quantity:            number;
    unit:                any;
    applicationAmount:   number;
    totalPrice:          number;
    year:                number | null;
    startMonth:          number | null;
    endMonth:            number | null;
}

@Component({
    selector: 'app-budget-line-item-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS
    ],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './budget-line-item-add-edit.component.html'
})
export class BudgetLineItemAddEditComponent {
    module    = 'Budget Line Item';
    subModule = 'Create';
    menuLink  = 'budget-line-item';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    submit     = false;
    isLoading  = signal(false);

    year:                 number = new Date().getFullYear() + 1;
    forSupplementalBudget = false;
    selectedDepartment:   any = null;
    selectedDivision:     any = null;

    departments  = signal<any[]>([]);
    divisions    = signal<any[]>([]);
    units        = signal<any[]>([]);
    projectTypes = signal<any[]>([]);
    strategicInitiatives = signal<any[]>([]);

    checkedBy:        any = null;
    verifiedBy:       any = null;
    approvingOfficer: any = null;

    details: BliDetail[] = [];

    readonly months = [
        { id: 1, name: 'January' }, { id: 2, name: 'February' }, { id: 3, name: 'March' },
        { id: 4, name: 'April' },   { id: 5, name: 'May' },      { id: 6, name: 'June' },
        { id: 7, name: 'July' },    { id: 8, name: 'August' },   { id: 9, name: 'September' },
        { id: 10, name: 'October' },{ id: 11, name: 'November' },{ id: 12, name: 'December' }
    ];

    readonly implementationYears: number[] = (() => {
        const cur = new Date().getFullYear();
        return [cur - 1, cur, cur + 1, cur + 2, cur + 3];
    })();

    private service      = inject(BudgetLineItemService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadReferenceData();
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
                this.addDetail();
                this.loadUserDefaults();
                this.loadDefaultSignatories();
            }
        });
    }

    loadReferenceData(): void {
        this.service.getDepartments().subscribe({ next: (d) => this.departments.set(d || []), error: () => {} });
        this.service.getDivisions().subscribe({ next: (d) => this.divisions.set(d || []), error: () => {} });
        this.service.getUnits().subscribe({ next: (d) => this.units.set(d || []), error: () => {} });
        this.service.getProjectTypes().subscribe({ next: (d) => this.projectTypes.set(d || []), error: () => {} });
        this.service.getStrategicInitiatives().subscribe({ next: (d) => this.strategicInitiatives.set(d || []), error: () => {} });
    }

    loadUserDefaults(): void {
        this.service.getUserDepartment().subscribe({ next: (d) => { this.selectedDepartment = d || null; }, error: () => {} });
        this.service.getUserDivision().subscribe({ next: (d) => { this.selectedDivision = d || null; }, error: () => {} });
    }

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.checkedBy        = data.checkedBy        || null;
                    this.verifiedBy       = data.verifiedBy       || null;
                    this.approvingOfficer = data.approvedBy       || data.approvingOfficer || null;
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
                    this.year                  = data.year || new Date().getFullYear();
                    this.forSupplementalBudget = data.forSupplementalBudget || false;
                    this.selectedDepartment    = data.department || null;
                    this.selectedDivision      = data.division   || null;
                    this.checkedBy             = data.checkedBy  || null;
                    this.verifiedBy            = data.verifiedBy || null;
                    this.approvingOfficer      = data.approvingOfficer || null;
                    this.details = (data.budgetLineItemDetails || []).map((d: any) => ({
                        id:                  d.id,
                        title:               d.title || '',
                        projectType:         d.projectType || null,
                        location:            d.location || '',
                        length:              Number(d.length) || 0,
                        strategicInitiative: d.strategicInitiative || null,
                        item:                d.item || null,
                        specification:       d.specification || '',
                        quantity:            Number(d.quantity) || 0,
                        unit:                d.unit || null,
                        applicationAmount:   Number(d.applicationAmount || d.finalAmount) || 0,
                        totalPrice:          Number(d.totalPrice) || 0,
                        year:                d.year || null,
                        startMonth:          d.startMonth || null,
                        endMonth:            d.endMonth || null
                    }));
                    if (this.details.length === 0) this.addDetail();
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

    addDetail(): void {
        this.details.push({
            id: null, title: '', projectType: this.projectTypes()[0] || null,
            location: '', length: 0, strategicInitiative: null, item: null,
            specification: '', quantity: 0, unit: null, applicationAmount: 0, totalPrice: 0,
            year: null, startMonth: null, endMonth: null
        });
    }

    removeDetail(index: number): void {
        if (this.details.length > 1) this.details.splice(index, 1);
    }

    calcTotal(d: BliDetail): void {
        d.totalPrice = (Number(d.quantity) || 0) * (Number(d.applicationAmount) || 0);
    }

    async openItemBrowse(index: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseItemModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                const item = result.data;
                this.details[index].item = item;
                this.service.getNeaPriceByItem(item.id).subscribe({
                    next: (npi: any) => {
                        if (npi) {
                            this.details[index].specification     = npi.neaPriceIndex?.description || 'N/A';
                            this.details[index].applicationAmount = Number(npi.price) || 0;
                        } else {
                            this.details[index].specification     = 'N/A';
                            this.details[index].applicationAmount = 0;
                        }
                        this.calcTotal(this.details[index]);
                    },
                    error: () => { this.details[index].specification = 'N/A'; this.details[index].applicationAmount = 0; }
                });
            }
        } catch { }
    }

    removeItem(index: number): void {
        this.details[index].item = null;
    }

    async openSignatoryBrowse(field: 'checkedBy' | 'verifiedBy' | 'approvingOfficer'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this[field] = result.data;
            }
        } catch { }
    }

    clearSignatory(field: 'checkedBy' | 'verifiedBy' | 'approvingOfficer'): void {
        this[field] = null;
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    isValid(): boolean {
        return !!(this.year && this.details.length > 0 && this.checkedBy);
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) return;

        this.formSubmit = true;
        const payload: any = {
            id:                    this.editMode ? this.id : null,
            year:                  this.year,
            forSupplementalBudget: this.forSupplementalBudget,
            division:              this.selectedDivision      ? { id: this.selectedDivision.id }      : null,
            department:            this.selectedDepartment    ? { id: this.selectedDepartment.id }    : null,
            checkedBy:             this.checkedBy             ? { id: this.checkedBy.id }             : null,
            verifiedBy:            this.verifiedBy            ? { id: this.verifiedBy.id }            : null,
            approvingOfficer:      this.approvingOfficer      ? { id: this.approvingOfficer.id }      : null,
            budgetLineItemDetails: this.details.map(d => ({
                id:                  d.id || null,
                title:               d.title,
                projectType:         d.projectType ? { id: d.projectType.id } : null,
                location:            d.location || null,
                length:              Number(d.length) || 0,
                strategicInitiative: d.strategicInitiative ? { id: d.strategicInitiative.id } : null,
                item:                d.item ? { id: d.item.id } : null,
                specification:       d.specification || null,
                quantity:            Number(d.quantity) || 0,
                unit:                d.unit ? { id: d.unit.id } : null,
                applicationAmount:   Number(d.applicationAmount) || 0,
                totalPrice:          Number(d.totalPrice) || 0,
                year:                d.year || null,
                startMonth:          d.startMonth || null,
                endMonth:            d.endMonth || null
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
