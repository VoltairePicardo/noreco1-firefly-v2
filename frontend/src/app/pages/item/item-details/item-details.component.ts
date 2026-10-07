import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { FormBuilder, FormsModule, ReactiveFormsModule, UntypedFormGroup, Validators } from '@angular/forms';
import { ItemService } from '../item.service';
import { WorkflowService } from '@/app/shared/workflow/workflow.service';
import { DocumentLogsComponent } from '@/app/shared/components/document-logs/document-logs.component';
import { documentStatusBadgeClass } from '@/app/shared/workflow/document-status-badge.util';
import { buildProcessPayload, WorkflowActionOption } from '@/app/models/shared/workflow.model';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseCOAModalComponent } from '@/app/shared/modals/browse-coa-modal/browse-coa-modal.component';
import { BrowseItemModalComponent } from '@/app/shared/modals/browse-item-modal/browse-item-modal.component';
import { AuthService } from '@/app/pages/auth/auth.service';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-item-details',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, FormsModule, ReactiveFormsModule, DocumentLogsComponent],
    templateUrl: './item-details.component.html'
})
export class ItemDetailsComponent implements OnInit {
    module    = 'Item';
    subModule = 'Details';
    menuLink  = 'item';

    data       = signal<any>(null);
    isLoading  = signal(false);
    actions    = signal<WorkflowActionOption[]>([]);
    processing = signal(false);

    currentUserId: number | null = null;
    isInventoryOfficer = signal(false);

    // Approval form state
    units            = signal<any[]>([]);
    categories       = signal<any[]>([]);
    showApprovalForm = signal(false);
    approvalForm     = signal<UntypedFormGroup | null>(null);
    selectedAssetAccount   = signal<any>(null);
    selectedExpenseAccount = signal<any>(null);
    selectedParentItem     = signal<any>(null);
    duplicates             = signal<any[]>([]);

    selectedAction: WorkflowActionOption | null = null;
    remarks = '';

    readonly statusBadgeClass = documentStatusBadgeClass;

    private route           = inject(ActivatedRoute);
    private router          = inject(Router);
    private service         = inject(ItemService);
    private alertService    = inject(AlertService);
    private workflowService = inject(WorkflowService);
    private fb              = inject(FormBuilder);
    private modalService    = inject(ModalService);
    private authService     = inject(AuthService);

    ngOnInit(): void {
        this.currentUserId = this.authService.getUser()?.user?.id ?? null;
        this.service.isInventoryOfficer().subscribe({ next: v => this.isInventoryOfficer.set(v), error: () => {} });
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            const id = idParam != null && /^\d+$/.test(idParam) ? Number(idParam) : null;
            if (id !== null) { this.load(id); }
        });
    }

    onActionChange(): void {
        if (this.selectedAction?.actionId === 5) { // APPROVE
            this.showApprovalForm.set(true);
            this.initApprovalForm();
            if (this.units().length === 0) {
                this.service.listUnits().subscribe({ next: d => this.units.set(d), error: () => {} });
            }
            if (this.categories().length === 0) {
                this.service.listCategories().subscribe({ next: d => this.categories.set(d), error: () => {} });
            }
        } else {
            this.showApprovalForm.set(false);
            this.approvalForm.set(null);
            this.duplicates.set([]);
        }
    }

    private initApprovalForm(): void {
        const item = this.data();
        this.selectedParentItem.set(item?.parentItem ?? null);
        this.selectedAssetAccount.set(item?.assetAccount ? {
            id: item.assetAccount.id,
            accountCode:  item.assetAccount.code,
            accountTitle: item.assetAccount.title
        } : null);
        this.selectedExpenseAccount.set(item?.expenseAccount ? {
            id: item.expenseAccount.id,
            accountCode:  item.expenseAccount.code,
            accountTitle: item.expenseAccount.title
        } : null);

        const form = this.fb.group({
            code:                [item?.code                  || ''],
            description:         [item?.description           || '', Validators.required],
            unitId:              [item?.unit?.id              || null, Validators.required],
            reorderPoint:        [item?.reorderPoint          ?? null],
            idealQty:            [item?.idealQty              ?? null],
            location:            [item?.location              || ''],
            isActive:            [item?.isActive              ?? true],
            inventoryCategoryId: [item?.inventoryCategory?.id || null],
            hasSerialNumbers:    [item?.hasSerialNumbers      ?? false],
            barcode:             [item?.barcode               || ''],
        });

        form.get('description')!.valueChanges.pipe(
            debounceTime(400),
            distinctUntilChanged(),
            switchMap((q: string) => q?.trim().length > 1
                ? this.service.list(q, null, 0, 5, null, item?.id)
                : [{ content: [] }])
        ).subscribe({ next: (res: any) => this.duplicates.set(res.content ?? []), error: () => {} });

        this.approvalForm.set(form);
    }

    async openAssetBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) { this.selectedAssetAccount.set(result.data); }
        } catch {}
    }

    async openExpenseBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseCOAModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) { this.selectedExpenseAccount.set(result.data); }
        } catch {}
    }

    async openParentBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseItemModalComponent,
                { excludeId: this.data()?.id },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) { this.selectedParentItem.set(result.data); }
        } catch {}
    }

    processWorkflow(): void {
        if (!this.selectedAction) return;
        const item = this.data();
        if (!item?.id) return;

        if (this.selectedAction.actionId === 5) { // APPROVE — update item first, then process
            const form = this.approvalForm();
            if (!form || form.invalid) {
                form?.markAllAsTouched();
                Swal.fire({ title: 'Required', text: 'Please fill in all required fields (Description, Unit).', icon: 'warning' });
                return;
            }
            if (!this.selectedAssetAccount()) {
                Swal.fire({ title: 'Required', text: 'Asset Account is required for approval.', icon: 'warning' });
                return;
            }
            if (!this.selectedExpenseAccount()) {
                Swal.fire({ title: 'Required', text: 'Expense Account is required for approval.', icon: 'warning' });
                return;
            }

            this.processing.set(true);
            const v = form.value;
            const payload = {
                id:                item.id,
                code:              v.code,
                description:       v.description,
                unit:              v.unitId              ? { id: v.unitId }                      : null,
                reorderPoint:      v.reorderPoint,
                idealQty:          v.idealQty,
                location:          v.location,
                isActive:          v.isActive,
                parentItem:        this.selectedParentItem()     ? { id: this.selectedParentItem().id }     : null,
                assetAccount:      { id: this.selectedAssetAccount().id },
                expenseAccount:    { id: this.selectedExpenseAccount().id },
                inventoryCategory: v.inventoryCategoryId ? { id: v.inventoryCategoryId }        : null,
                hasSerialNumbers:  v.hasSerialNumbers,
                barcode:           v.barcode,
                // not editable here — pass through so approval doesn't blank them
                subCategory:       item.subCategory ? { id: item.subCategory.id } : null,
                genericName:       item.genericName,
                size:              item.size,
                rating:            item.rating,
                specification:     item.specification,
                brand:             item.brand ? { id: item.brand.id } : null,
                manufacturer:      item.manufacturer,
                partNumber:        item.partNumber,
                remarks:           item.remarks,
            };

            this.service.update(payload).subscribe({
                next: (res) => {
                    if (!res.success) {
                        this.processing.set(false);
                        Swal.fire({ title: 'Error', text: res.failureMessage, icon: 'error' });
                        return;
                    }
                    this.service.process(buildProcessPayload(item.id, this.selectedAction!.actionMapId, this.remarks)).subscribe({
                        next: (pRes) => {
                            this.processing.set(false);
                            if (pRes.success) {
                                Swal.fire({ title: 'Approved', text: 'Item saved and approved.', icon: 'success', timer: 2000, showConfirmButton: false });
                                this.selectedAction = null;
                                this.showApprovalForm.set(false);
                                this.approvalForm.set(null);
                                this.remarks = '';
                                this.load(item.id);
                            } else {
                                Swal.fire({ title: 'Error', text: pRes.failureMessage, icon: 'error' });
                            }
                        },
                        error: () => { this.processing.set(false); Swal.fire({ title: 'Error', text: 'Failed to process.', icon: 'error' }); }
                    });
                },
                error: () => { this.processing.set(false); Swal.fire({ title: 'Error', text: 'Failed to update item.', icon: 'error' }); }
            });

        } else { // All other actions (Send for Approval, Disapprove, etc.)
            this.processing.set(true);
            this.service.process(buildProcessPayload(item.id, this.selectedAction.actionMapId, this.remarks)).subscribe({
                next: (res) => {
                    this.processing.set(false);
                    if (res.success) {
                        Swal.fire({ title: 'Success', text: res.successMessage, icon: 'success', timer: 2000, showConfirmButton: false });
                        this.selectedAction = null;
                        this.remarks = '';
                        this.load(item.id);
                    } else {
                        Swal.fire({ title: 'Error', text: res.failureMessage, icon: 'error' });
                    }
                },
                error: () => {
                    this.processing.set(false);
                    Swal.fire({ title: 'Error', text: 'Failed to process document.', icon: 'error' });
                }
            });
        }
    }

    private load(id: number): void {
        this.isLoading.set(true);
        this.service.getData(id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data.set(data);
                    this.actions.set([]);
                    if (data.transaction?.id) {
                        this.workflowService.getAvailableActionsForTransaction(data.transaction.id).subscribe({
                            next: (acts) => this.actions.set(acts ?? [])
                        });
                    }
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }
}
