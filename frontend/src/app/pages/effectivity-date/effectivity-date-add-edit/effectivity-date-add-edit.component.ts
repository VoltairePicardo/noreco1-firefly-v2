import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { EffectivityDateService } from '../effectivity-date.service';

@Component({
    selector: 'app-effectivity-date-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule, FlatpickrModule],
    providers: [FlatpickrDefaults],
    templateUrl: './effectivity-date-add-edit.component.html'
})
export class EffectivityDateAddEditComponent {
    module     = 'Effectivity Date';
    subModule  = 'Create';
    menuLink   = 'effectivity-date';
    id: any    = 0;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };
    isLoading  = signal(false);
    validationForm!: UntypedFormGroup;

    private service      = inject(EffectivityDateService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);

    ngOnInit(): void {
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

    private toDateInputValue(isoDate: string | null | undefined): string {
        if (!isoDate) return '';
        return isoDate.substring(0, 10);
    }

    initForm(data?: any): void {
        this.validationForm = this.fb.group({
            start:       [data?.start ? this.toDateInputValue(data.start) : '', Validators.required],
            end:         [data?.end   ? this.toDateInputValue(data.end)   : '', Validators.required],
            description: [data?.description || ''],
        }, { validators: this.dateOrderValidator });
    }

    private dateOrderValidator(group: UntypedFormGroup): { [key: string]: any } | null {
        const start = group.get('start')?.value;
        const end   = group.get('end')?.value;
        if (start && end && new Date(end) <= new Date(start)) {
            return { endBeforeStart: true };
        }
        return null;
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

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid || this.validationForm.errors?.['endBeforeStart']) { this.formSubmit = false; return; }

        const frm = { id: this.id || 0, ...this.form.value };
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
