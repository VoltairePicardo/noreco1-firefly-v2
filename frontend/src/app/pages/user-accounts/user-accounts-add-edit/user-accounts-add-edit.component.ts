import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { LaddaModule } from 'angular2-ladda';
import { UserAccountsService } from '../user-accounts.service';
import { DepartmentService } from '../../department/department.service';
import { DivisionService } from '../../division/division.service';
import { SectionService } from '../../section/section.service';
import { PositionService } from '../../position/position.service';
import { RolesService } from '../../roles/roles.service';

@Component({
    selector: 'app-user-accounts-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS, LaddaModule],
    templateUrl: './user-accounts-add-edit.component.html'
})
export class UserAccountsAddEditComponent {
    module    = 'User Account';
    subModule = 'Create';
    menuLink  = 'user-accounts';
    id: any   = 0;
    editMode  = false;
    submit    = false;
    formSubmit = false;
    isLoading     = signal(false);
    showPassword  = signal(false);
    validationForm!: UntypedFormGroup;

    departments  = signal<any[]>([]);
    allDivisions = signal<any[]>([]);
    allSections  = signal<any[]>([]);
    allPositions = signal<any[]>([]);
    rolesList: { id: number, name: string, selected: boolean }[] = [];
    signatureFile: File | null        = null;
    signaturePreview: string | null   = null;
    currentSignatureUrl: string | null = null;

    filteredDivisions = computed(() => {
        const deptId = this.validationForm?.get('departmentId')?.value;
        return deptId ? this.allDivisions().filter(d => d.department?.id === deptId) : this.allDivisions();
    });

    filteredSections = computed(() => {
        const divId = this.validationForm?.get('divisionId')?.value;
        return divId ? this.allSections().filter(s => s.division?.id === divId) : this.allSections();
    });

    filteredPositions = computed(() => {
        const deptId = this.validationForm?.get('departmentId')?.value;
        const divId  = this.validationForm?.get('divisionId')?.value;
        const secId  = this.validationForm?.get('sectionId')?.value;
        return this.allPositions().filter(p =>
            (!deptId || p.department?.id === deptId) &&
            (!divId  || p.division?.id  === divId)  &&
            (!secId  || p.section?.id   === secId)
        );
    });

    private service           = inject(UserAccountsService);
    private departmentService = inject(DepartmentService);
    private divisionService   = inject(DivisionService);
    private sectionService    = inject(SectionService);
    private positionService   = inject(PositionService);
    private rolesService      = inject(RolesService);
    public  fb                = inject(FormBuilder);
    public  route             = inject(ActivatedRoute);
    public  router            = inject(Router);
    public  alertService      = inject(AlertService);

    ngOnInit(): void {
        this.departmentService.list().subscribe({ next: d => this.departments.set(d),  error: () => {} });
        this.divisionService.list().subscribe({  next: d => this.allDivisions.set(d),  error: () => {} });
        this.sectionService.list().subscribe({   next: d => this.allSections.set(d),   error: () => {} });
        this.positionService.list().subscribe({  next: d => this.allPositions.set(d),  error: () => {} });
        this.rolesService.list().subscribe({
            next: (roles) => {
                this.rolesList = roles.map(r => ({ id: r.id, name: r.name, selected: false }));
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
        if (data?.roles?.length) {
            const assignedIds = new Set(data.roles.map((r: any) => r.id));
            this.rolesList = this.rolesList.map(r => ({ ...r, selected: assignedIds.has(r.id) }));
        }
        this.validationForm = this.fb.group({
            lastName:       [data?.lastName      || '', [Validators.required]],
            firstName:      [data?.firstName     || '', [Validators.required]],
            middleName:     [data?.middleName    || ''],
            extensionName:  [data?.extensionName || ''],
            username:       [data?.username      || '', Validators.required],
            email:          [data?.email         || '', [Validators.required, Validators.email]],
            password:       ['', this.editMode ? [] : [Validators.required]],
            retypePassword: ['', this.editMode ? [] : [Validators.required]],
            enabled:        [data?.enabled       ?? true],
            departmentId:   [data?.department?.id || null],
            divisionId:     [data?.division?.id   || null],
            sectionId:      [data?.section?.id    || null],
            positionId:     [data?.position?.id   || null],
        });
    }

    getData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next:  (data) => {
                this.isLoading.set(false);
                if (data?.id) { this.currentSignatureUrl = data.signatureUrl || null; this.initForm(data); }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: (err: any) => { this.isLoading.set(false); this.alertService.httpError(this.module, 'Error', err); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    onDeptChange(): void {
        this.validationForm.patchValue({ divisionId: null, sectionId: null, positionId: null });
    }

    onDivisionChange(): void {
        this.validationForm.patchValue({ sectionId: null, positionId: null });
    }

    onSectionChange(): void {
        this.validationForm.patchValue({ positionId: null });
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    onSignatureSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files?.length) {
            this.signatureFile = input.files[0];
            const reader = new FileReader();
            reader.onload = (e) => { this.signaturePreview = e.target?.result as string; };
            reader.readAsDataURL(this.signatureFile);
        }
    }

    removeSignature(): void {
        this.signatureFile    = null;
        this.signaturePreview = null;
    }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const v = this.form.value;
        const frm: any = {
            id:            this.editMode ? this.id : null,
            lastName:      v.lastName,
            firstName:     v.firstName,
            middleName:    v.middleName,
            extensionName: v.extensionName,
            username:      v.username,
            email:         v.email,
            enabled:       v.enabled,
            department:    v.departmentId ? { id: v.departmentId } : null,
            division:      v.divisionId   ? { id: v.divisionId }   : null,
            section:       v.sectionId    ? { id: v.sectionId }    : null,
            position:      v.positionId   ? { id: v.positionId }   : null,
            roles:         this.rolesList.filter(r => r.selected).map(r => ({ id: r.id })),
        };
        if (v.password) {
            frm.password       = v.password;
            frm.retypePassword = v.retypePassword;
        }

        const req = this.editMode ? this.service.update(frm) : this.service.create(frm);

        req.subscribe({
            next: (res) => {
                if (res.success) {
                    const afterSave = () => {
                        this.alertService.success(this.module, 'Saved', '');
                        this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                    };
                    if (this.signatureFile) {
                        this.service.uploadSignature(this.signatureFile, res.modelId).subscribe({
                            next:  () => afterSave(),
                            error: () => afterSave()
                        });
                    } else {
                        afterSave();
                    }
                } else {
                    this.formSubmit = false;
                    const msg = res.messages?.length ? res.messages.join(', ') : (res.failureMessage || '');
                    this.alertService.error(this.module, 'Saving', msg);
                }
            },
            error: (err: any) => { this.formSubmit = false; this.alertService.httpError(this.module, 'Saving', err); }
        });
    }
}
