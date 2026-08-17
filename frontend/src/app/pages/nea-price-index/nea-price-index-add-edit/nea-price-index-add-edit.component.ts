import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { NeaPriceIndexService } from '../nea-price-index.service';

@Component({
    selector: 'app-nea-price-index-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule, FlatpickrModule],
    providers: [FlatpickrDefaults],
    templateUrl: './nea-price-index-add-edit.component.html'
})
export class NeaPriceIndexAddEditComponent {
    module     = 'NEA Price Index';
    subModule  = 'Create';
    menuLink   = 'nea-price-index';
    id: any    = 0;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);
    validationForm!: UntypedFormGroup;

    flatpickrOptions      = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    details: any[]        = [];
    allItems              = signal<any[]>([]);
    inventoryCategories   = signal<any[]>([]);
    selectedCategoryId: any = null;

    private service      = inject(NeaPriceIndexService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);

    ngOnInit(): void {
        this.service.listInventoryCategories().subscribe({ next: d => this.inventoryCategories.set(d), error: () => {} });

        this.service.listItems().subscribe({
            next: (data: any) => {
                const items = data.content ?? data;
                this.allItems.set(items);
            },
            error: () => {}
        });

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

    initForm(data?: any): void {
        this.validationForm = this.fb.group({
            effectivityDate: [data?.effectivityDate ? new Date(data.effectivityDate).toISOString().substring(0, 10) : '', Validators.required],
            description:     [data?.description || ''],
        });
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.initForm(data);
                    this.details = (data.neaPriceIndexDetails || []).map((d: any) => ({
                        item:            { id: d.item.id },
                        itemCode:        d.item.code       || '',
                        unitCode:        d.item.unit?.code || '',
                        itemDescription: d.item.description,
                        price:           d.price
                    }));
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    onCategoryChange(categoryId: any): void {
        if (!categoryId) { this.details = []; return; }
        const filtered = this.allItems().filter((item: any) => item.inventoryCategory?.id == categoryId);
        this.details = filtered.map((item: any) => ({
            item:            { id: item.id },
            itemCode:        item.code        || item.itemCode        || '',
            unitCode:        item.unit?.code  || item.unitCode        || '',
            itemDescription: item.description || item.itemDescription || '',
            price:           null
        }));
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const v = this.form.value;
        const frm: any = {
            id:              this.editMode ? this.id : null,
            effectivityDate: v.effectivityDate,
            description:     v.description,
            neaPriceIndexDetails: this.details.map(d => ({
                item:  { id: d.item.id },
                price: d.price ?? 0
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
