import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { ItemService } from '@/app/pages/item/item.service';
import { WorkflowService } from '@/app/shared/workflow/workflow.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { buildProcessPayload } from '@/app/models/shared/workflow.model';
import { provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerArrowLeft } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-item-quick-create-modal',
    standalone: true,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerCheck, tablerArrowLeft })],
    templateUrl: './item-quick-create-modal.component.html',
})
export class ItemQuickCreateModalComponent implements OnInit {
    @Input() parentItemId!: number;
    @Input() parentItemDescription!: string;

    units      = signal<any[]>([]);
    categories = signal<any[]>([]);
    isInventoryOfficer = signal(false);
    isLoading  = signal(false);

    description = '';
    code        = '';
    unitId: number | null = null;
    inventoryCategoryId: number | null = null;
    reorderPoint: number | null = null;
    idealQty: number | null = null;
    location    = '';
    barcode     = '';
    hasSerialNumbers = false;
    isActive    = true;

    activeModal          = inject(NgbActiveModal);
    private itemService  = inject(ItemService);
    private wfService    = inject(WorkflowService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.itemService.listUnits().subscribe({ next: (d: any) => this.units.set(d?.content ?? d ?? []), error: () => {} });
        this.itemService.listCategories().subscribe({ next: (d: any) => this.categories.set(d?.content ?? d ?? []), error: () => {} });
        this.itemService.isInventoryOfficer().subscribe({ next: v => this.isInventoryOfficer.set(v), error: () => {} });
    }

    private buildPayload(): any {
        return {
            id:                0,
            code:              this.code,
            description:       this.description,
            unit:              this.unitId              ? { id: this.unitId }              : null,
            reorderPoint:      this.reorderPoint,
            idealQty:          this.idealQty,
            location:          this.location,
            isActive:          this.isActive,
            parentItem:        this.parentItemId        ? { id: this.parentItemId }        : null,
            inventoryCategory: this.inventoryCategoryId ? { id: this.inventoryCategoryId } : null,
            hasSerialNumbers:  this.hasSerialNumbers,
            barcode:           this.barcode,
            assetAccount:      null,
            expenseAccount:    null,
        };
    }

    save(andApprove = false): void {
        if (!this.description?.trim()) {
            this.alertService.warning('Item', 'Validation', 'Description is required.');
            return;
        }
        if (!this.unitId) {
            this.alertService.warning('Item', 'Validation', 'Unit of Measure is required.');
            return;
        }
        this.isLoading.set(true);
        this.itemService.create(this.buildPayload()).subscribe({
            next: (res: any) => {
                if (!res?.success) {
                    this.isLoading.set(false);
                    this.alertService.error('Item', 'Save', res?.failureMessage || '');
                    return;
                }
                const modelId: number = res.modelId;
                if (andApprove && this.isInventoryOfficer()) {
                    this.approveAndClose(modelId);
                } else {
                    this.itemService.getData(modelId).subscribe({
                        next: (item: any) => {
                            this.isLoading.set(false);
                            this.activeModal.close({
                                id:         item.id ?? modelId,
                                description: item.description ?? this.description,
                                isApproved: false,
                            });
                        },
                        error: () => {
                            this.isLoading.set(false);
                            this.activeModal.close({ id: modelId, description: this.description, isApproved: false });
                        }
                    });
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error('Item', 'Save', '');
            }
        });
    }

    private approveAndClose(modelId: number): void {
        this.itemService.getData(modelId).subscribe({
            next: (item: any) => {
                const transId: number | undefined = item?.transaction?.id;
                if (!transId) {
                    this.isLoading.set(false);
                    this.activeModal.close({ id: item.id ?? modelId, description: item.description ?? this.description, isApproved: false });
                    return;
                }
                this.wfService.getAvailableActionsForTransaction(transId).subscribe({
                    next: (actions: any[]) => {
                        const approveAction = (actions ?? []).find(
                            a => (a.action ?? '').toLowerCase().includes('approv')
                        );
                        if (!approveAction) {
                            this.isLoading.set(false);
                            this.activeModal.close({ id: item.id, description: item.description, isApproved: false });
                            return;
                        }
                        this.itemService.process(buildProcessPayload(item.id, approveAction.actionMapId, '')).subscribe({
                            next: (pRes: any) => {
                                this.isLoading.set(false);
                                this.activeModal.close({ id: item.id, description: item.description, isApproved: pRes?.success === true });
                            },
                            error: () => {
                                this.isLoading.set(false);
                                this.activeModal.close({ id: item.id, description: item.description, isApproved: false });
                            }
                        });
                    },
                    error: () => {
                        this.isLoading.set(false);
                        this.activeModal.close({ id: item.id, description: item.description, isApproved: false });
                    }
                });
            },
            error: () => {
                this.isLoading.set(false);
                this.activeModal.close({ id: modelId, description: this.description, isApproved: false });
            }
        });
    }
}
