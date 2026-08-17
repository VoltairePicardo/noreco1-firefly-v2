import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { PositionService } from '../position.service';
import { DepartmentService } from '../../department/department.service';
import { DivisionService } from '../../division/division.service';
import { SectionService } from '../../section/section.service';

@Component({
    selector: 'app-position-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule],
    templateUrl: './position-add-edit.component.html'
})
export class PositionAddEditComponent {
    module    = 'Position';
    subModule = 'Create';
    menuLink  = 'position';
    id: any   = 0;
    editMode  = false;
    submit    = false;
    formSubmit = false;
    isLoading = signal(false);
    validationForm!: UntypedFormGroup;

    departments = signal<any[]>([]);
    allDivisions = signal<any[]>([]);
    allSections  = signal<any[]>([]);

    filteredDivisions = computed(() => {
        const deptId = this.validationForm?.get('departmentId')?.value;
        return deptId ? this.allDivisions().filter(d => d.department?.id === deptId) : this.allDivisions();
    });

    filteredSections = computed(() => {
        const divId = this.validationForm?.get('divisionId')?.value;
        return divId ? this.allSections().filter(s => s.division?.id === divId) : this.allSections();
    });

    private service           = inject(PositionService);
    private departmentService = inject(DepartmentService);
    private divisionService   = inject(DivisionService);
    private sectionService    = inject(SectionService);
    public  fb                = inject(FormBuilder);
    public  route             = inject(ActivatedRoute);
    public  router            = inject(Router);
    public  alertService      = inject(AlertService);

    ngOnInit(): void {
        this.departmentService.list().subscribe({ next: d => this.departments.set(d), error: () => {} });
        this.divisionService.list().subscribe({ next: d => this.allDivisions.set(d), error: () => {} });
        this.sectionService.list().subscribe({ next: d => this.allSections.set(d), error: () => {} });

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
            code:         [data?.code              || '', Validators.required],
            name:         [data?.name              || '', Validators.required],
            departmentId: [data?.department?.id    || null],
            divisionId:   [data?.division?.id      || null],
            sectionId:    [data?.section?.id       || null],
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

    onDeptChange(): void {
        this.validationForm.patchValue({ divisionId: null, sectionId: null });
    }

    onDivisionChange(): void {
        this.validationForm.patchValue({ sectionId: null });
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const v = this.form.value;
        const frm = {
            id:         this.id || 0,
            code:       v.code,
            name:       v.name,
            department: v.departmentId ? { id: v.departmentId } : null,
            division:   v.divisionId   ? { id: v.divisionId }   : null,
            section:    v.sectionId    ? { id: v.sectionId }    : null,
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
