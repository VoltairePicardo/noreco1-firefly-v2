import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { SectionService } from '../section.service';
import { DivisionService } from '../../division/division.service';

@Component({
    selector: 'app-section-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule],
    templateUrl: './section-add-edit.component.html'
})
export class SectionAddEditComponent {
    module    = 'Section';
    subModule = 'Create';
    menuLink  = 'section';
    id: any   = 0;
    editMode  = false;
    submit    = false;
    formSubmit = false;
    isLoading = signal(false);
    validationForm!: UntypedFormGroup;

    divisions = signal<any[]>([]);

    private service         = inject(SectionService);
    private divisionService = inject(DivisionService);
    public  fb              = inject(FormBuilder);
    public  route           = inject(ActivatedRoute);
    public  router          = inject(Router);
    public  alertService    = inject(AlertService);

    ngOnInit(): void {
        this.divisionService.list().subscribe({
            next: (data) => this.divisions.set(data),
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
            name:       [data?.name           || '', Validators.required],
            divisionId: [data?.division?.id   || null, Validators.required],
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
        const frm = {
            id:       this.id || 0,
            name:     v.name,
            division: v.divisionId ? { id: v.divisionId } : null
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
