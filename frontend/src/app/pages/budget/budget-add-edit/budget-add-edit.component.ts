import { Component, ElementRef, HostListener, inject, QueryList, signal, ViewChildren } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerPlus, tablerArrowLeft, tablerCheck, tablerTrash, tablerEdit } from '@ng-icons/tabler-icons';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowseCashflowItemModalComponent } from '@/app/shared/modals/browse-cashflow-item-modal/browse-cashflow-item-modal.component';
import { BudgetService } from '../budget.service';

@Component({
    selector: 'app-budget-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
    ],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerPlus, tablerArrowLeft, tablerCheck, tablerTrash, tablerEdit })],
    templateUrl: './budget-add-edit.component.html'
})
export class BudgetAddEditComponent {
    module    = 'Cash Flow Budget';
    subModule = 'Create';
    menuLink  = 'budget';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    // Form fields
    year = '';

    // Budget details: { cashflowItem: any, amount: number }
    budgetDetails: any[] = [];

    // Lookup data
    cashflowItems = signal<any[]>([]);

    private service      = inject(BudgetService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modal        = inject(NgbModal);

    @ViewChildren('amountInput') amountInputs!: QueryList<ElementRef>;

    ngOnInit(): void {
        this.loadCashflowItems();
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
            }
        });
    }

    loadCashflowItems(): void {
        this.service.getCashflowItems().subscribe({
            next: (data) => this.cashflowItems.set(data || []),
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                if (data?.id) {
                    this.year = data.year ? String(data.year) : '';
                    this.loadDetailsForEdit();
                } else {
                    this.isLoading.set(false);
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

    loadDetailsForEdit(): void {
        this.service.getDetails(this.id).subscribe({
            next: (details) => {
                this.isLoading.set(false);
                this.budgetDetails = (details || []).map((d: any) => ({
                    cashflowItem: d.cashflowItem || null,
                    amount: d.amount || 0
                }));
            },
            error: () => {
                this.isLoading.set(false);
            }
        });
    }

    openCashflowBrowse(index?: number): void {
        const ref = this.modal.open(BrowseCashflowItemModalComponent, { size: 'lg', centered: true, scrollable: true });
        ref.componentInstance.items = this.cashflowItems();
        ref.result.then(result => {
            if (result?.action === 'select') {
                const item = result.data;
                if (index === undefined) {
                    this.budgetDetails.push({ cashflowItem: item, amount: 0 });
                } else {
                    this.budgetDetails[index] = { ...this.budgetDetails[index], cashflowItem: item };
                }
                const targetIndex = index !== undefined ? index : this.budgetDetails.length - 1;
                setTimeout(() => {
                    const inputs = this.amountInputs.toArray();
                    if (inputs[targetIndex]) {
                        inputs[targetIndex].nativeElement.focus();
                        inputs[targetIndex].nativeElement.select();
                    }
                }, 50);
            }
        }, () => {});
    }

    private lastEnterTime = 0;

    @HostListener('document:keydown', ['$event'])
    onGlobalKeydown(event: KeyboardEvent): void {
        if (event.key !== '+') return;
        const tag = (document.activeElement as HTMLElement)?.tagName?.toLowerCase();
        if (tag !== 'input' && tag !== 'textarea' && tag !== 'select') {
            event.preventDefault();
            this.openCashflowBrowse();
        }
    }

    onAmountKeydown(event: KeyboardEvent): void {
        if (event.key === '+') {
            event.preventDefault();
            event.stopPropagation();
            this.openCashflowBrowse();
        } else if (event.key === 'Enter') {
            this.onAmountEnter();
        }
    }

    onAmountEnter(): void {
        const now = Date.now();
        if (now - this.lastEnterTime < 400) {
            this.lastEnterTime = 0;
            this.save();
        } else {
            this.lastEnterTime = now;
        }
    }

    removeDetail(index: number): void {
        this.budgetDetails.splice(index, 1);
    }

    get totalAmount(): number {
        return this.budgetDetails.reduce((sum, d) => sum + (Number(d.amount) || 0), 0);
    }

    clampYear(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.value.length > 4) {
            this.year = input.value.slice(0, 4);
            input.value = this.year;
        }
    }

    trackByIndex(index: number): number {
        return index;
    }

    save(): void {
        if (!this.year || isNaN(Number(this.year))) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a valid year.');
            return;
        }
        if (this.budgetDetails.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one budget detail.');
            return;
        }
        const invalidDetail = this.budgetDetails.find(d => !d.cashflowItem || Number(d.amount) <= 0);
        if (invalidDetail) {
            this.alertService.warning(this.module, 'Validation', 'All details must have a cashflow item and an amount greater than 0.');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            id:            this.editMode ? this.id : null,
            year:          Number(this.year),
            budgetDetails: this.budgetDetails.map(d => ({
                cashflowItem: { id: d.cashflowItem?.id },
                amount:       Number(d.amount) || 0
            }))
        };

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req$.subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Saved successfully.', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred while saving.', '');
            }
        });
    }
}
