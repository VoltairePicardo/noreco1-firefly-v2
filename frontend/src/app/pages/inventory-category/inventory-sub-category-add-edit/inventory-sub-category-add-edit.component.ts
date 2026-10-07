import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { InventorySubCategory } from '@/app/models/coop-accounting/inventory-category.model';
import { InventoryCategoryService } from '../inventory-category.service';
import { InventorySubCategoryService } from '../inventory-sub-category.service';

@Component({
    selector: 'app-inventory-sub-category-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS],
    changeDetection: ChangeDetectionStrategy.OnPush,
    templateUrl: './inventory-sub-category-add-edit.component.html'
})
export class InventorySubCategoryAddEditComponent {
    module              = 'Inventory Sub Category';
    subModule           = signal('Create');
    menuLink            = 'inventory-category';
    categoryId          = 0;
    id                  = 0;
    editMode            = false;
    categoryDescription = signal('');
    isLoading           = signal(false);
    submitted           = signal(false);
    saving              = signal(false);
    form!: FormGroup;

    private service         = inject(InventorySubCategoryService);
    private categoryService = inject(InventoryCategoryService);
    private fb              = inject(FormBuilder);
    private route           = inject(ActivatedRoute);
    private router          = inject(Router);
    private alertService    = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.categoryId = +(params.get('categoryId') ?? 0);
            const id = params.get('id');
            this.editMode = id != null && /^\d+$/.test(id);
            this.initForm();
            if (this.editMode) {
                this.id = +id!;
                this.subModule.set('Edit');
                this.getData();
            } else {
                this.subModule.set('Create');
                this.getCategory();
            }
        });
    }

    initForm(data?: InventorySubCategory): void {
        this.form = this.fb.group({
            description: [data?.description ?? '', Validators.required]
        });
    }

    getCategory(): void {
        this.categoryService.getData(this.categoryId).subscribe({
            next:  (category) => this.categoryDescription.set(category?.description ?? ''),
            error: () => this.goToCategory('Error')
        });
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.categoryId = data.categoryId ?? this.categoryId;
                    this.categoryDescription.set(data.categoryDescription ?? '');
                    this.initForm(data);
                } else { this.goToCategory('Not Found'); }
            },
            error: () => { this.isLoading.set(false); this.goToCategory('Error'); }
        });
    }

    private goToCategory(action: string): void {
        this.alertService.error(this.module, action, '');
        this.router.navigate(['/' + this.menuLink, this.categoryId, 'detail']);
    }

    invalid(control: string): boolean {
        return this.submitted() && !!this.form.get(control)?.errors;
    }

    validSubmit(): void {
        this.submitted.set(true);
        if (this.form.invalid) { return; }

        this.saving.set(true);
        const frm: InventorySubCategory = {
            id: this.editMode ? this.id : null,
            categoryId: this.categoryId,
            description: this.form.value.description
        };
        const req = this.editMode ? this.service.update(frm) : this.service.create(frm);

        req.subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, this.categoryId, 'detail']);
                } else {
                    this.saving.set(false);
                    this.alertService.error(this.module, 'Saving', res.failureMessage);
                }
            },
            error: () => { this.saving.set(false); this.alertService.error(this.module, 'Saving', ''); }
        });
    }
}
