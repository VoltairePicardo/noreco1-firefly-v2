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
import { provideIcons } from '@ng-icons/core';
import {
    tablerPlus, tablerTrash, tablerArrowLeft, tablerDeviceFloppy, tablerSearch, tablerX
} from '@ng-icons/tabler-icons';
import { CostEstimateService } from '../cost-estimate.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseProjectModalComponent } from '@/app/shared/modals/browse-project-modal/browse-project-modal.component';
import { BrowseAssemblyUnitModalComponent } from '@/app/shared/modals/browse-assembly-unit-modal/browse-assembly-unit-modal.component';
import { BrowseItemModalComponent } from '@/app/shared/modals/browse-item-modal/browse-item-modal.component';
import { BrowseMiscChargeModalComponent } from '@/app/shared/modals/browse-misc-charge-modal/browse-misc-charge-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';

export const CE_TYPES = [
    { id: 1, description: 'Outsourced' },
    { id: 2, description: 'Insourced' },
    { id: 3, description: 'To be determined' },
];

@Component({
    selector: 'app-cost-estimate-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerPlus, tablerTrash, tablerArrowLeft, tablerDeviceFloppy, tablerSearch, tablerX })
    ],
    templateUrl: './cost-estimate-add-edit.component.html'
})
export class CostEstimateAddEditComponent {
    module    = 'Cost Estimate';
    subModule = 'Create';
    menuLink  = 'cost-estimate';

    id: any    = null;
    editMode   = false;
    isLoading  = signal(false);
    formSubmit = false;

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Header fields
    date = '';

    // Inventory Location
    inventoryLocations = signal<any[]>([]);
    selectedInventoryLocation: any = null;

    // CE Types (hardcoded)
    ceTypes = CE_TYPES;
    selectedType: any = CE_TYPES[0];

    // Project
    project: any = null;

    // Assemblies — browse-selected from BrowseAssemblyUnitModal
    assemblies: {
        code: string;
        description: string;
        laborCost: number;
        isCalculated: boolean;
        quantity: number;
    }[] = [];

    // Accessories — browse-selected from BrowseItemModal
    detailsAccess: {
        code: string;
        description: string;
        unitCost: number;
        quantity: number;
    }[] = [];

    // Metering — browse-selected from BrowseItemModal
    detailsMeter: {
        code: string;
        description: string;
        unitCost: number;
        quantity: number;
    }[] = [];

    // Misc Charges — browse-selected from BrowseMiscChargeModal
    miscCharges: {
        description: string;
        unitCost: number;
        quantity: number;
        remarks: string;
    }[] = [];

    // Labor Cost
    laborCostPercentage    = 0;
    isTotalLaborCalculated = true;
    totalLaborCostManual   = 0;

    // Freight & Handling
    freightHandlingPercentage   = 0;
    isFreightHandlingCalculated = true;
    freightHandlingManual       = 0;

    // Contingency
    contingencyPercentage   = 0;
    isContingencyCalculated = true;
    contingencyManual       = 0;

    // Notes
    notes = '';

    // Signatories — entity objects
    signatories: { [key: string]: any } = {
        concurredBy:   null,
        recommendedBy: null,
        approvedBy:    null,
    };
    signatoryKeys   = ['concurredBy', 'recommendedBy', 'approvedBy'];
    signatoryLabels: { [key: string]: string } = {
        concurredBy:   'Concurred By',
        recommendedBy: 'Recommended By',
        approvedBy:    'Approved By',
    };

    private service      = inject(CostEstimateService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    ngOnInit(): void {
        this.loadInventoryLocations();
        this.loadDefaultSettings();
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

    loadInventoryLocations(): void {
        this.service.getInventoryLocations().subscribe({
            next: (data) => { this.inventoryLocations.set(data || []); },
            error: () => {}
        });
    }

    loadDefaultSettings(): void {
        this.service.getSetting('LABOR_COST_PERC').subscribe({
            next: (v) => { if (v != null) this.laborCostPercentage = Number(v) || 0; },
            error: () => {}
        });
        this.service.getSetting('FREIGHT_HAND_PERC').subscribe({
            next: (v) => { if (v != null) this.freightHandlingPercentage = Number(v) || 0; },
            error: () => {}
        });
        this.service.getSetting('CONTINGENCY_PERC').subscribe({
            next: (v) => { if (v != null) this.contingencyPercentage = Number(v) || 0; },
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.date          = data.date ? new Date(data.date).toISOString().substring(0, 10) : '';
                    this.project       = data.project       || null;
                    this.notes = data.notes || '';
                    this.signatories = {
                        concurredBy:   data.concurredBy   || null,
                        recommendedBy: data.recommendedBy || null,
                        approvedBy:    data.approvedBy    || null,
                    };

                    // Inventory location
                    if (data.inventoryLocation?.id) {
                        const found = this.inventoryLocations().find(l => l.id === data.inventoryLocation.id);
                        this.selectedInventoryLocation = found || data.inventoryLocation;
                    }

                    // Type
                    if (data.type?.id || data.typeText) {
                        this.selectedType = CE_TYPES.find(t =>
                            t.id === data.type?.id || t.description === data.typeText
                        ) ?? CE_TYPES[0];
                    }

                    // Labor/freight/contingency
                    this.laborCostPercentage    = Number(data.laborCostPercentage)    || this.laborCostPercentage;
                    this.isTotalLaborCalculated = data.isTotalLaborCalculated  ?? true;
                    this.totalLaborCostManual   = Number(data.laborCost)        || 0;

                    this.freightHandlingPercentage   = Number(data.freightHandlingPercentage)   || this.freightHandlingPercentage;
                    this.isFreightHandlingCalculated = data.isFreightHandlingCalculated ?? true;
                    this.freightHandlingManual       = Number(data.freightHandling)     || 0;

                    this.contingencyPercentage   = Number(data.contingencyPercentage)   || this.contingencyPercentage;
                    this.isContingencyCalculated = data.isContingencyCalculated ?? true;
                    this.contingencyManual       = Number(data.contingency)     || 0;

                    // Assemblies
                    this.assemblies = (data.assemblies || data.costEstimateAssemblyUnits || []).map((a: any) => ({
                        code:         a.assemblyUnit?.code        || a.code        || '',
                        description:  a.assemblyUnit?.description || a.description || '',
                        laborCost:    Number(a.laborCost || a.unitCost)  || 0,
                        isCalculated: a.isCalculated ?? true,
                        quantity:     Number(a.quantity) || 0,
                    }));

                    // Accessories
                    this.detailsAccess = (data.detailsAccess || []).map((d: any) => ({
                        code:        d.itemCode     || d.code        || '',
                        description: d.itemDescription || d.description || '',
                        unitCost:    Number(d.unitCost)  || 0,
                        quantity:    Number(d.quantity)  || 0,
                    }));

                    // Metering
                    this.detailsMeter = (data.detailsMeter || []).map((d: any) => ({
                        code:        d.itemCode     || d.code        || '',
                        description: d.itemDescription || d.description || '',
                        unitCost:    Number(d.unitCost)  || 0,
                        quantity:    Number(d.quantity)  || 0,
                    }));

                    // Misc Charges
                    this.miscCharges = (data.miscCharges || data.miscellaneousCharges || []).map((m: any) => ({
                        description: m.miscellaneousCharge?.description || m.description || '',
                        unitCost:    Number(m.unitCost) || 0,
                        quantity:    Number(m.quantity) || 0,
                        remarks:     m.remarks          || ''
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

    // ── Project ──────────────────────────────────────────────────────────────────
    async browseProject(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseProjectModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select') this.project = result.data;
        } catch {}
    }

    // ── Assemblies ───────────────────────────────────────────────────────────────
    async browseAssemblyUnit(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseAssemblyUnitModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result.data) {
                const a = result.data;
                this.assemblies.push({
                    code:         a.code         || '',
                    description:  a.description  || '',
                    laborCost:    Number(a.laborCost) || 0,
                    isCalculated: true,
                    quantity:     1
                });
            }
        } catch {}
    }

    removeAssembly(i: number): void { this.assemblies.splice(i, 1); }

    // ── Accessories ──────────────────────────────────────────────────────────────
    async browseAccessoryItem(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseItemModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result.data) {
                const item = result.data;
                this.detailsAccess.push({
                    code:        item.code        || '',
                    description: item.description || item.name || '',
                    unitCost:    Number(item.unitCost || item.price || 0),
                    quantity:    1
                });
            }
        } catch {}
    }

    removeDetailAccess(i: number): void { this.detailsAccess.splice(i, 1); }

    // ── Metering ─────────────────────────────────────────────────────────────────
    async browseMeterItem(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseItemModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result.data) {
                const item = result.data;
                this.detailsMeter.push({
                    code:        item.code        || '',
                    description: item.description || item.name || '',
                    unitCost:    Number(item.unitCost || item.price || 0),
                    quantity:    1
                });
            }
        } catch {}
    }

    removeDetailMeter(i: number): void { this.detailsMeter.splice(i, 1); }

    // ── Misc Charges ─────────────────────────────────────────────────────────────
    async browseMiscCharge(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseMiscChargeModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result.data) {
                const m = result.data;
                this.miscCharges.push({
                    description: m.description || '',
                    unitCost:    Number(m.amount || 0),
                    quantity:    1,
                    remarks:     ''
                });
            }
        } catch {}
    }

    removeMiscCharge(i: number): void { this.miscCharges.splice(i, 1); }

    // ── Signatories ──────────────────────────────────────────────────────────────
    async openEntityBrowse(key: string): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select') this.signatories[key] = result.data;
        } catch {}
    }

    clearSignatory(key: string): void { this.signatories[key] = null; }

    // ── Totals ───────────────────────────────────────────────────────────────────
    get totalMaterialCost(): number {
        return this.detailsAccess.reduce((s, d) => s + (d.quantity * d.unitCost), 0);
    }
    get totalMeteringCost(): number {
        return this.detailsMeter.reduce((s, d) => s + (d.quantity * d.unitCost), 0);
    }
    get totalMiscCost(): number {
        return this.miscCharges.reduce((s, m) => s + (m.unitCost * m.quantity), 0);
    }
    get totalAssemblyLaborCost(): number {
        return this.assemblies
            .filter(a => a.isCalculated)
            .reduce((s, a) => s + (a.laborCost * a.quantity), 0);
    }
    get totalLaborCost(): number {
        if (this.isTotalLaborCalculated) return this.totalMaterialCost * this.laborCostPercentage / 100;
        return this.totalLaborCostManual;
    }
    get freightHandling(): number {
        if (this.isFreightHandlingCalculated) return this.totalMaterialCost * this.freightHandlingPercentage / 100;
        return this.freightHandlingManual;
    }
    get contingency(): number {
        if (this.isContingencyCalculated) {
            return (this.totalMaterialCost + this.totalMeteringCost + this.totalMiscCost) * this.contingencyPercentage / 100;
        }
        return this.contingencyManual;
    }
    get grandTotal(): number {
        return this.totalMaterialCost + this.totalMeteringCost + this.totalMiscCost
             + this.totalAssemblyLaborCost + this.totalLaborCost + this.freightHandling + this.contingency;
    }

    compareById(a: any, b: any): boolean { return a?.id === b?.id; }

    // ── Save ─────────────────────────────────────────────────────────────────────
    save(): void {
        if (!this.date) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            date:              this.date,
            project:           this.project ? { id: this.project.id } : null,
            inventoryLocation: this.selectedInventoryLocation ? { id: this.selectedInventoryLocation.id } : null,
            type:              this.selectedType ? { id: this.selectedType.id } : null,
            typeText:          this.selectedType?.description || null,
            notes:         this.notes || null,
            concurredBy:   this.signatories['concurredBy']   ? { accountNo: this.signatories['concurredBy'].accountNo }   : null,
            recommendedBy: this.signatories['recommendedBy'] ? { accountNo: this.signatories['recommendedBy'].accountNo } : null,
            approvedBy:    this.signatories['approvedBy']    ? { accountNo: this.signatories['approvedBy'].accountNo }    : null,

            laborCostPercentage:         this.laborCostPercentage,
            isTotalLaborCalculated:      this.isTotalLaborCalculated,
            laborCost:                   this.totalLaborCost,

            freightHandlingPercentage:   this.freightHandlingPercentage,
            isFreightHandlingCalculated: this.isFreightHandlingCalculated,
            freightHandling:             this.freightHandling,

            contingencyPercentage:       this.contingencyPercentage,
            isContingencyCalculated:     this.isContingencyCalculated,
            contingency:                 this.contingency,

            totalMaterialCost:        this.totalMaterialCost,
            totalMeteringCost:        this.totalMeteringCost,
            totalMiscellaneousCharge: this.totalMiscCost,
            totalAssemblyLaborCost:   this.totalAssemblyLaborCost,
            grandTotal:               this.grandTotal,

            assemblies:    this.assemblies,
            detailsAccess: this.detailsAccess,
            detailsMeter:  this.detailsMeter,
            miscCharges:   this.miscCharges,
        };

        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode ? this.service.update(payload) : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                this.formSubmit = false;
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(this.module, this.editMode ? 'Updated' : 'Created', '');
                    const id = data?.modelId ?? data?.id ?? this.id;
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'Save', '');
            }
        });
    }
}
