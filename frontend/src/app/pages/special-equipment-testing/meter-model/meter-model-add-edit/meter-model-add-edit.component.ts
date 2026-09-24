import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { MeterModelService } from '../meter-model.service';

@Component({
    selector: 'app-meter-model-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule],
    templateUrl: './meter-model-add-edit.component.html'
})
export class MeterModelAddEditComponent {
    module     = 'Meter Model';
    subModule  = 'Create';
    menuLink   = 'meter-model';
    id: any    = 0;
    editMode   = false;
    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);
    validationForm!: UntypedFormGroup;

    brands          = signal<any[]>([]);
    meterTypes      = signal<any[]>([]);
    phases          = signal<any[]>([]);
    currents        = signal<any[]>([]);
    accuracyClasses = signal<any[]>([]);
    meterForms      = signal<any[]>([]);

    private service      = inject(MeterModelService);
    public  fb           = inject(FormBuilder);
    public  route        = inject(ActivatedRoute);
    public  router       = inject(Router);
    public  alertService = inject(AlertService);

    ngOnInit(): void {
        this.service.listBrands().subscribe({ next: d => this.brands.set(d), error: () => {} });
        this.service.listMeterTypes().subscribe({ next: d => this.meterTypes.set(d), error: () => {} });
        this.service.listPhases().subscribe({ next: d => this.phases.set(d), error: () => {} });
        this.service.listCurrents().subscribe({ next: d => this.currents.set(d), error: () => {} });
        this.service.listAccuracyClasses().subscribe({ next: d => this.accuracyClasses.set(d), error: () => {} });
        this.service.listMeterForms().subscribe({ next: d => this.meterForms.set(d), error: () => {} });

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
            modelName:       [data?.modelName          || '', Validators.required],
            brandId:         [data?.brand?.id          || null, Validators.required],
            meterTypeId:     [data?.meterType?.id      || null, Validators.required],
            phaseId:         [data?.phase?.id          || null],
            currentRatingId: [data?.currentRating?.id  || null],
            accuracyClassId: [data?.accuracyClass?.id  || null],
            meterFormId:     [data?.meterForm?.id      || null],
            constant:        [data?.constant           ?? null],
            amperage:        [data?.amperage           ?? null],
            voltage:         [data?.voltage             ?? null],
        });
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
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const v = this.form.value;
        const frm: any = {
            id:            this.id || 0,
            modelName:     v.modelName,
            brand:         v.brandId         ? { id: v.brandId }         : null,
            meterType:     v.meterTypeId     ? { id: v.meterTypeId }     : null,
            phase:         v.phaseId         ? { id: v.phaseId }         : null,
            currentRating: v.currentRatingId ? { id: v.currentRatingId } : null,
            accuracyClass: v.accuracyClassId ? { id: v.accuracyClassId } : null,
            meterForm:     v.meterFormId     ? { id: v.meterFormId }     : null,
            constant:      v.constant,
            amperage:      v.amperage,
            voltage:       v.voltage,
        };

        const req = this.editMode ? this.service.update(frm) : this.service.create(frm);

        req.subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Saved', '');
                    this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                } else {
                    this.formSubmit = false;
                    if (res.messages?.length) {
                        this.alertService.fieldWarning(this.module, 'Saving', res.messages);
                    } else {
                        this.alertService.error(this.module, 'Saving', res.failureMessage);
                    }
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'Saving', ''); }
        });
    }
}
