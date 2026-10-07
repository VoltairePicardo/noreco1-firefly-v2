import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { QuotationService } from '../quotation.service';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseRvItemsModalComponent } from '@/app/shared/modals/browse-rv-items-modal/browse-rv-items-modal.component';
import { BrowseSupplierModalComponent } from '@/app/shared/modals/browse-supplier-modal/browse-supplier-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerPlus, tablerTrash, tablerArrowLeft, tablerDeviceFloppy, tablerX, tablerCheck } from '@ng-icons/tabler-icons';
import { ItemQuickCreateModalComponent } from '../item-quick-create-modal/item-quick-create-modal.component';
import { forkJoin } from 'rxjs';

@Component({
    selector: 'app-quotation-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        LaddaModule,
        FlatpickrDirective,
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS, provideIcons({ tablerSearch, tablerPlus, tablerTrash, tablerArrowLeft, tablerDeviceFloppy, tablerX, tablerCheck })],
    templateUrl: './quotation-add-edit.component.html'
})
export class QuotationAddEditComponent {
    module    = 'Quotation';
    menuLink  = 'quotation';
    id: any   = 0;
    editMode  = false;
    subModule = 'Create';

    isLoading  = signal(false);
    formSubmit = false;
    submitted  = false;

    flatpickrOptions     = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    readonlyDateOptions  = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y', clickOpens: false, allowInput: false };

    compareBrand(a: any, b: any): boolean { return a?.id === b?.id; }

    numericOnly(event: Event, obj: any, key: string): void {
        const val = (event.target as HTMLInputElement).value.replace(/[^0-9.]/g, '');
        obj[key] = val;
        (event.target as HTMLInputElement).value = val;
    }

    quotationDate = '';
    particular    = '';

    suppliers: (any | null)[] = [null, null, null];

    terms: any[] = [
        { deliveryTimeAndCompletion: '', warrantyPeriod: '', termsOfPayment: null, placeOfDelivery: '' },
        { deliveryTimeAndCompletion: '', warrantyPeriod: '', termsOfPayment: null, placeOfDelivery: '' },
        { deliveryTimeAndCompletion: '', warrantyPeriod: '', termsOfPayment: null, placeOfDelivery: '' },
    ];

    lineItems: any[] = [];
    brands: any[]    = [];

    approvingOfficer: any = null;
    generalManager:   any = null;
    isTotalMoreThan100k   = false;

    readonly supplierSlots = [0, 1, 2];

    private service      = inject(QuotationService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    private toLocalDateStr(d: Date): string {
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    }

    ngOnInit(): void {
        this.quotationDate = this.toLocalDateStr(new Date());

        this.service.getBrands().subscribe({
            next: (res: any) => { this.brands = res.content ?? res ?? []; },
            error: () => {}
        });

        this.route.paramMap.subscribe(params => {
            this.editMode = params.get('id') != null && /^\d+$/.test(params.get('id') ?? '');
            if (this.editMode) {
                this.id        = params.get('id');
                this.subModule = 'Edit';
                this.loadForEdit();
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        forkJoin({
            header:  this.service.getData(this.id),
            details: this.service.getDetails(this.id)
        }).subscribe({
            next: ({ header, details }: any) => {
                this.isLoading.set(false);
                if (header?.id) {
                    this.quotationDate = header.date ? this.toLocalDateStr(new Date(header.date)) : '';
                    this.particular    = header.particular || '';

                    const sups = header.suppliers || [];
                    this.suppliers = [sups[0] ?? null, sups[1] ?? null, sups[2] ?? null];

                    const loadedTerms: any[] = header.terms || [];
                    loadedTerms.forEach((t: any, i: number) => {
                        if (i < 3) {
                            this.terms[i] = {
                                deliveryTimeAndCompletion: t.deliveryTimeAndCompletion || '',
                                warrantyPeriod:            t.warrantyPeriod            || '',
                                termsOfPayment:            t.termsOfPayment            ?? null,
                                placeOfDelivery:           t.placeOfDelivery           || '',
                            };
                        }
                    });

                    if (header.approvingOfficerObj) this.approvingOfficer = header.approvingOfficerObj;
                    if (header.generalManagerObj)   { this.generalManager = header.generalManagerObj; this.isTotalMoreThan100k = true; }

                    this.lineItems = (details || []).map((d: any) => ({
                        rvDetailId:         d.purchaseRequestDetailId,
                        rvNumber:           d.rvNo || '',
                        itemDescription:    d.itemDescription || '',
                        unitCode:           d.unitCode || '',
                        quantity:           d.quantity || 0,
                        available:          d.available ?? true,
                        originalItemId:     d.originalItemId ?? null,
                        isChildItem:        d.isChildItem ?? false,
                        isApprovedItem:     d.isApprovedItem ?? false,
                        newItemId:          d.newItemId ?? null,
                        newItemDescription: null,
                        details: [
                            { brand: d.details?.[0]?.brand || null, price: d.details?.[0]?.price || 0, awarded: d.details?.[0]?.awarded || false },
                            { brand: d.details?.[1]?.brand || null, price: d.details?.[1]?.price || 0, awarded: d.details?.[1]?.awarded || false },
                            { brand: d.details?.[2]?.brand || null, price: d.details?.[2]?.price || 0, awarded: d.details?.[2]?.awarded || false },
                        ]
                    }));
                    this.recalcTotal();
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load quotation.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    async openPrBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseRvItemsModalComponent,
                { type: 'quotation' },
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select' && result?.data?.length) {
                const existing = new Set(this.lineItems.map((li: any) => li.rvDetailId));
                const newItems = result.data.filter((item: any) => !existing.has(item.id));
                newItems.forEach((item: any) => {
                    this.lineItems.push({
                        rvDetailId:         item.id,
                        rvNumber:           item.rvNumber || '',
                        itemDescription:    item.itemDescription || '',
                        unitCode:           item.unitCode || '',
                        quantity:           item.quantity || 0,
                        available:          true,
                        originalItemId:     item.itemId ?? null,
                        isChildItem:        item.parentItemId != null,
                        isApprovedItem:     false,
                        newItemId:          null,
                        newItemDescription: null,
                        details: [
                            { brand: null, price: 0, awarded: false },
                            { brand: null, price: 0, awarded: false },
                            { brand: null, price: 0, awarded: false },
                        ]
                    });
                });

                // Auto-copy PR particulars: collect unique purposes for newly added PRs only
                const existingRvNumbers = new Set<string>(
                    this.lineItems.slice(0, this.lineItems.length - newItems.length).map((li: any) => li.rvNumber)
                );
                const purposeByRv = new Map<string, string>(); // rvNumber → purpose
                newItems.forEach((item: any) => {
                    if (item.rvPurpose && item.rvNumber && !existingRvNumbers.has(item.rvNumber) && !purposeByRv.has(item.rvNumber)) {
                        purposeByRv.set(item.rvNumber, item.rvPurpose);
                    }
                });
                if (purposeByRv.size > 0) {
                    const newPurposes = Array.from(purposeByRv.values()).join('; ');
                    this.particular = this.particular.trim()
                        ? `${this.particular.trim()}; ${newPurposes}`
                        : newPurposes;
                }
            }
        } catch { }
    }

    removeLineItem(index: number): void {
        this.lineItems.splice(index, 1);
        this.recalcTotal();
    }

    async openItemModal(rowIndex: number): Promise<void> {
        const line = this.lineItems[rowIndex];
        try {
            const result = await this.modalService.openModal(
                ItemQuickCreateModalComponent,
                {
                    parentItemId:          line.originalItemId ?? null,
                    parentItemDescription: line.itemDescription,
                },
                { size: 'xl', centered: true }
            );
            if (result?.id) {
                this.lineItems[rowIndex].newItemId          = result.id;
                this.lineItems[rowIndex].newItemDescription = result.description;
                this.lineItems[rowIndex].isChildItem        = true;
                this.lineItems[rowIndex].isApprovedItem     = result.isApproved ?? false;
                this.lineItems[rowIndex].itemDescription    = result.description;
            }
        } catch { }
    }

    async openSupplierBrowse(index: number): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseSupplierModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.suppliers[index] = result.data;
            }
        } catch { }
    }

    clearSupplier(index: number): void {
        this.suppliers[index] = null;
    }

    async openSignatoryBrowse(field: 'approvingOfficer' | 'generalManager'): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                (this as any)[field] = result.data;
            }
        } catch { }
    }

    awardSupplier(itemIdx: number, supplierIdx: number): void {
        if (this.lineItems[itemIdx].details[supplierIdx].awarded) {
            for (let i = 0; i < 3; i++) {
                if (i !== supplierIdx) this.lineItems[itemIdx].details[i].awarded = false;
            }
        }
        this.recalcTotal();
    }

    recalcTotal(): void {
        let total = 0;
        for (const item of this.lineItems) {
            if (!item.available) continue;
            for (let i = 0; i < 3; i++) {
                if (item.details[i]?.awarded) total += (item.details[i].price || 0) * (item.quantity || 0);
            }
        }
        this.isTotalMoreThan100k = total > 100000;
        if (!this.isTotalMoreThan100k) this.generalManager = null;
    }

    get totalAmount(): number {
        let total = 0;
        for (const item of this.lineItems) {
            const prices = this.supplierSlots
                .filter(i => this.suppliers[i])
                .map(i => item.details[i]?.price || 0)
                .filter((p: number) => p > 0);
            const lowest = prices.length > 0 ? Math.min(...prices) : 0;
            total += lowest * (item.quantity || 0);
        }
        return total;
    }

    save(): void {
        this.submitted = true;

        if (!this.quotationDate) { this.alertService.warning(this.module, 'Please provide a quotation date.', ''); return; }
        if (!this.particular?.trim()) { this.alertService.warning(this.module, 'Please provide particulars.', ''); return; }
        if (this.lineItems.length === 0) { this.alertService.warning(this.module, 'Please add at least one item.', ''); return; }
        if (!this.approvingOfficer) { this.alertService.warning(this.module, 'Please select a Finance Manager.', ''); return; }
        if (this.isTotalMoreThan100k && !this.generalManager) { this.alertService.warning(this.module, 'Total exceeds ₱100,000 — please select a General Manager.', ''); return; }

        this.formSubmit = true;

        const activeSuppliers = this.supplierSlots
            .map(i => ({ supplier: this.suppliers[i], index: i }))
            .filter(x => x.supplier !== null);

        const payload: any = {
            date:      this.quotationDate,
            particular: this.particular,
            suppliers:  this.suppliers.filter(s => s !== null),
            quotationDetails: this.lineItems.map(item => ({
                purchaseRequestDetailId: item.rvDetailId,
                available: item.available ?? true,
                newItemId: item.newItemId ?? null,
                details: activeSuppliers.map(x => ({
                    supplier: { id: x.supplier.id, accountNumber: x.supplier.accountNumber },
                    brand:    item.details[x.index]?.brand ? { id: item.details[x.index].brand.id } : null,
                    price:    item.details[x.index]?.price  || 0,
                    awarded:  item.details[x.index]?.awarded || false,
                }))
            })),
            terms: activeSuppliers.map(x => ({
                supplier:                  { id: x.supplier.id, accountNumber: x.supplier.accountNumber },
                deliveryTimeAndCompletion: this.terms[x.index]?.deliveryTimeAndCompletion || '',
                warrantyPeriod:            this.terms[x.index]?.warrantyPeriod            || '',
                termsOfPayment:            this.terms[x.index]?.termsOfPayment            ?? null,
                placeOfDelivery:           this.terms[x.index]?.placeOfDelivery           || '',
            })),
            approvingOfficer:         this.approvingOfficer ? { accountNo: this.approvingOfficer.accountNo } : null,
            approvedByGeneralManager: this.generalManager   ? { accountNo: this.generalManager.accountNo }   : null,
        };

        if (this.editMode) payload.id = this.id;

        const req = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req.subscribe({
            next: (res: any) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, this.editMode ? 'Updated' : 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.alertService.error(this.module, res?.failureMessage || 'Save failed.', '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred.', '');
            }
        });
    }
}
