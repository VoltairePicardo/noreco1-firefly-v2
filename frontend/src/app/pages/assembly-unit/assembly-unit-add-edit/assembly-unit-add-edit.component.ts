import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { NgbTooltip } from '@ng-bootstrap/ng-bootstrap';
import { AssemblyUnitService } from '../assembly-unit.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseItemModalComponent } from '@/app/shared/modals/browse-item-modal/browse-item-modal.component';

@Component({
    selector: 'app-assembly-unit-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule, NgbTooltip],
    templateUrl: './assembly-unit-add-edit.component.html'
})
export class AssemblyUnitAddEditComponent {
    module     = 'Assembly Unit';
    subModule  = 'Create';
    menuLink   = 'assembly-unit';
    id: any    = 0;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);
    assemblyTypes = signal<any[]>([]);
    lineItems: any[] = [];
    validationForm!: UntypedFormGroup;

    private service      = inject(AssemblyUnitService);
    private modalService = inject(ModalService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadTypes();
        this.route.paramMap.subscribe(params => {
            this.editMode = params.get('id') != null && /^\d+$/.test(params.get('id') ?? '');
            if (this.editMode) {
                this.id = params.get('id');
                this.subModule = 'Edit';
                this.getData();
            } else {
                this.subModule = 'Create';
                this.initForm();
            }
        });
    }

    loadTypes(): void {
        this.service.getTypes().subscribe({
            next:  (data) => this.assemblyTypes.set(data),
            error: () => this.alertService.error(this.module, 'Load Types', '')
        });
    }

    initForm(data?: any): void {
        this.validationForm = this.fb.group({
            code:           [data?.code           || '', Validators.required],
            description:    [data?.description    || '', Validators.required],
            laborCost:      [data?.laborCost       ?? null],
            assemblyTypeId: [data?.assemblyType?.id ?? null, Validators.required],
        });
        this.lineItems = (data?.details || []).map((d: any) => ({
            itemId:      d.itemId,
            code:        d.code,
            description: d.description,
            unit:        d.unit,
            quantity:    d.quantity ?? 1,
        }));
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next:  (data) => {
                this.isLoading.set(false);
                if (data?.id) { this.initForm(data); }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    async openItemBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseItemModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                const item = result.data;
                const existing = new Set(this.lineItems.map((li: any) => li.itemId));
                if (!existing.has(item.id)) {
                    this.lineItems.push({
                        itemId:      item.id,
                        code:        item.code,
                        description: item.description,
                        unit:        item.unit?.code || item.unit || '',
                        quantity:    1,
                    });
                } else {
                    this.alertService.error(this.module, 'Item already added.', '');
                }
            }
        } catch { }
    }

    removeItem(index: number): void {
        this.lineItems.splice(index, 1);
    }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const v = this.form.value;
        const frm = {
            id:           this.editMode ? this.id : null,
            code:         v.code,
            description:  v.description,
            laborCost:    v.laborCost,
            assemblyType: { id: v.assemblyTypeId },
            details:      this.lineItems.map(li => ({
                itemId:   li.itemId,
                quantity: li.quantity,
            }))
        };
        const req = this.editMode ? this.service.update(frm) : this.service.create(frm);

        req.subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Saving', res.failureMessage);
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Saving', ''); }
        });
    }
}
