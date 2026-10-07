import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { InventoryCategory, InventoryCategoryType } from '@/app/models/coop-accounting/inventory-category.model';
import { InventoryCategoryService } from '../inventory-category.service';

@Component({
    selector: 'app-inventory-category-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS],
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './inventory-category-add-edit.component.html'
})
export class InventoryCategoryAddEditComponent {
    module     = 'Inventory Category';
    subModule  = signal('Create');
    menuLink   = 'inventory-category';
    id         = 0;
    editMode   = false;
    isLoading  = signal(false);
    submitted  = signal(false);
    saving     = signal(false);
    types      = signal<InventoryCategoryType[]>([]);
    form!: FormGroup;

    private service      = inject(InventoryCategoryService);
    private fb           = inject(FormBuilder);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.service.getTypes().subscribe({
            next:  (types) => this.types.set(types),
            error: () => this.alertService.error(this.module, 'Load Types', '')
        });

        this.route.paramMap.subscribe(params => {
            const id = params.get('id');
            this.editMode = id != null && /^\d+$/.test(id);
            if (this.editMode) {
                this.id = +id!;
                this.subModule.set('Edit');
                this.getData();
            } else {
                this.subModule.set('Create');
                this.initForm();
            }
        });
    }

    initForm(data?: InventoryCategory): void {
        this.form = this.fb.group({
            description: [data?.description ?? '', Validators.required],
            typeId:      [data?.type?.id ?? null, Validators.required]
        });
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) { this.initForm(data); }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    invalid(control: string): boolean {
        const c = this.form.get(control);
        return this.submitted() && !!c?.errors;
    }

    validSubmit(): void {
        this.submitted.set(true);
        if (this.form.invalid) { return; }

        this.saving.set(true);
        const { typeId, ...rest } = this.form.value;
        const frm: InventoryCategory = { id: this.editMode ? this.id : null, ...rest, type: { id: typeId } };
        const req = this.editMode ? this.service.update(frm) : this.service.create(frm);

        req.subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.saving.set(false);
                    this.alertService.error(this.module, 'Saving', res.failureMessage);
                }
            },
            error: () => { this.saving.set(false); this.alertService.error(this.module, 'Saving', ''); }
        });
    }
}
