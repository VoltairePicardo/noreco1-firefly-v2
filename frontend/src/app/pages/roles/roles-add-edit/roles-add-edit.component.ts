import { Component, inject, signal } from '@angular/core';
import { FormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ADD_EDIT_PAGE_IMPORTS, COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { RolesService } from '../roles.service';

@Component({
    selector: 'app-roles-add-edit',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_ADD_EDIT_PAGE_IMPORTS],
    templateUrl: './roles-add-edit.component.html'
})
export class RolesAddEditComponent {
    module    = 'Role';
    subModule = 'Create';
    menuLink  = 'roles';
    id: any   = 0;
    editMode  = false;
    submit    = false;
    formSubmit = false;
    isLoading  = signal(false);
    validationForm!: UntypedFormGroup;

    allMenus        = signal<any[]>([]);
    selectedMenuIds = signal<Set<number>>(new Set());

    private service      = inject(RolesService);
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
            } else {
                this.subModule = 'Create';
                this.initForm();
            }
            this.loadMenus();
        });
    }

    initForm(data?: any): void {
        this.validationForm = this.fb.group({
            name: [data?.name || '', [Validators.required, Validators.minLength(3), Validators.maxLength(512)]]
        });
    }

    loadMenus(): void {
        this.isLoading.set(true);
        this.service.getMenus().subscribe({
            next: (menus) => {
                this.allMenus.set(menus);
                if (this.editMode) {
                    this.loadRole();
                } else {
                    this.isLoading.set(false);
                }
            },
            error: () => { this.alertService.error(this.module, 'Load Menus', ''); this.isLoading.set(false); }
        });
    }

    loadRole(): void {
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.initForm(data);
                    const ids = new Set<number>((data.menus || []).map((m: any) => m.id as number));
                    this.selectedMenuIds.set(ids);
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    // — Menu selection helpers —

    isChecked(id: number): boolean {
        return this.selectedMenuIds().has(id);
    }

    isParentChecked(menu: any): boolean {
        return this.selectedMenuIds().has(menu.id);
    }

    toggleParent(menu: any, event: Event): void {
        const checked = (event.target as HTMLInputElement).checked;
        const next = new Set(this.selectedMenuIds());
        if (checked) {
            next.add(menu.id);
            (menu.subMenus || []).forEach((s: any) => next.add(s.id));
        } else {
            next.delete(menu.id);
            (menu.subMenus || []).forEach((s: any) => next.delete(s.id));
        }
        this.selectedMenuIds.set(next);
    }

    toggleChild(parent: any, childId: number, event: Event): void {
        const checked = (event.target as HTMLInputElement).checked;
        const next = new Set(this.selectedMenuIds());
        if (checked) {
            next.add(childId);
        } else {
            next.delete(childId);
            // uncheck parent if no other child remains checked
            const anyOtherChecked = (parent.subMenus || []).some((s: any) => s.id !== childId && next.has(s.id));
            if (!anyOtherChecked) next.delete(parent.id);
        }
        this.selectedMenuIds.set(next);
    }

    get parentMenus(): any[] {
        return this.allMenus()
            .filter(m => !m.parentMenu && m.id > 0)
            .map(m => ({ ...m, subMenus: (m.subMenus || []).filter((s: any) => s.id > 0) }));
    }

    get selectedCount(): number {
        return this.selectedMenuIds().size;
    }

    get form(): UntypedFormGroup { return this.validationForm; }

    validSubmit(): void {
        this.submit = true;
        this.formSubmit = true;
        if (this.validationForm.invalid) { this.formSubmit = false; return; }

        const frm: any = {
            id:                   this.id || 0,
            name:                 this.form.value.name,
            menus:                Array.from(this.selectedMenuIds()).map(id => ({ id })),
            menusToEvict:         [],
            pageComponents:       [],
            pageComponentsToEvict: []
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
