import { Component, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import {
    tablerPlus, tablerTrash, tablerArrowLeft, tablerDeviceFloppy,
    tablerSearch, tablerX, tablerPaperclip, tablerPhoto, tablerFile
} from '@ng-icons/tabler-icons';
import { ProjectService } from '../project.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseSupplierModalComponent } from '@/app/shared/modals/browse-supplier-modal/browse-supplier-modal.component';

export const REQUESTING_PARTY_TYPES = [
    { id: 1, description: 'Office' },
    { id: 2, description: 'Department' },
    { id: 3, description: 'Consumer/Employee' },
    { id: 4, description: 'Government' },
];

@Component({
    selector: 'app-project-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
    ],
    providers: [
        ...SHARED_PROVIDERS,
        provideIcons({ tablerPlus, tablerTrash, tablerArrowLeft, tablerDeviceFloppy, tablerSearch, tablerX, tablerPaperclip, tablerPhoto, tablerFile })
    ],
    templateUrl: './project-add-edit.component.html'
})
export class ProjectAddEditComponent {
    @ViewChild('stakingFileInput') stakingFileInput!: ElementRef<HTMLInputElement>;

    module    = 'Project';
    subModule = 'Create';
    menuLink  = 'project';

    id: any    = null;
    editMode   = false;
    isLoading  = signal(false);
    formSubmit = false;

    // Requesting party
    requestingPartyTypes = REQUESTING_PARTY_TYPES;
    selectedPartyType: any = null;

    // Office (party id=1)
    offices         = signal<any[]>([]);
    selectedOffice: any = null;

    // Department (party id=2)
    departments         = signal<any[]>([]);
    selectedDepartment: any = null;

    // Consumer/Employee (party id=3)
    consumerAccountNumber = '';
    consumerName          = '';

    // Government (party id=4)
    governmentOfficeName = '';

    // Core fields
    name           = '';
    location       = '';
    purpose        = '';
    paymentDetails = '';
    projectManager = '';

    // Contractors — browse-selected suppliers
    contractors: { accountNo: string; name: string }[] = [];

    // As Planned Staking Sheet
    stagedStakingFiles: File[] = [];
    existingFiles: any[] = [];

    private service      = inject(ProjectService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadOffices();
        this.loadDepartments();
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.editMode = idParam != null && /^\d+$/.test(idParam);
            if (this.editMode) {
                this.id        = Number(idParam);
                this.subModule = 'Edit';
                this.loadForEdit();
            } else {
                this.subModule = 'Create';
            }
        });
    }

    loadOffices(): void {
        this.service.getOffices().subscribe({
            next: (data) => { this.offices.set(data || []); this.tryPreselectOffice(); },
            error: () => {}
        });
    }

    loadDepartments(): void {
        this.service.getDepartments().subscribe({
            next: (data) => { this.departments.set(data || []); this.tryPreselectDepartment(); },
            error: () => {}
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.name           = data.name           || '';
                    this.location       = data.location       || '';
                    this.purpose        = data.purpose        || '';
                    this.paymentDetails = data.paymentDetails || '';
                    this.projectManager = data.projectManager || '';

                    // Requesting party — check old-style entity fields first
                    if (data.office?.id) {
                        this.selectedPartyType  = REQUESTING_PARTY_TYPES[0];
                        this._pendingOfficeId   = data.office.id;
                        this.tryPreselectOffice();
                    } else if (data.department?.id) {
                        this.selectedPartyType      = REQUESTING_PARTY_TYPES[1];
                        this._pendingDepartmentId   = data.department.id;
                        this.tryPreselectDepartment();
                    } else if (data.consumerName || data.consumerAccountNumber) {
                        this.selectedPartyType        = REQUESTING_PARTY_TYPES[2];
                        this.consumerAccountNumber    = data.consumerAccountNumber || '';
                        this.consumerName             = data.consumerName          || '';
                    } else if (data.governmentOfficeName) {
                        this.selectedPartyType    = REQUESTING_PARTY_TYPES[3];
                        this.governmentOfficeName = data.governmentOfficeName || '';
                    } else if (data.requestingPartyType) {
                        this.selectedPartyType = REQUESTING_PARTY_TYPES.find(
                            p => p.description === data.requestingPartyType
                        ) ?? null;
                    }

                    // Contractors
                    this.contractors = (data.contractors || []).map((c: any) => ({
                        accountNo: c.supplier?.accountNo || c.supplier?.accountNumber || c.accountNo || '',
                        name:      c.supplier?.name      || c.name                    || ''
                    }));

                    // Existing uploaded files
                    this.service.getFiles(this.id).subscribe({
                        next: (files) => { this.existingFiles = files || []; },
                        error: () => {}
                    });
                } else {
                    this.alertService.error(this.module, 'Record not found.', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Failed to load record.', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    // ── Pending preselect for offices/departments loaded async ──────────────────

    private _pendingOfficeId: number | null = null;
    private _pendingDepartmentId: number | null = null;

    tryPreselectOffice(): void {
        if (!this._pendingOfficeId) return;
        const found = this.offices().find(o => o.id === this._pendingOfficeId);
        if (found) { this.selectedOffice = found; this._pendingOfficeId = null; }
    }

    tryPreselectDepartment(): void {
        if (!this._pendingDepartmentId) return;
        const found = this.departments().find(d => d.id === this._pendingDepartmentId);
        if (found) { this.selectedDepartment = found; this._pendingDepartmentId = null; }
    }

    // ── Requesting party helpers ─────────────────────────────────────────────────

    compareById(a: any, b: any): boolean { return a?.id === b?.id; }

    get isOffice():     boolean { return this.selectedPartyType?.id === 1; }
    get isDepartment(): boolean { return this.selectedPartyType?.id === 2; }
    get isConsumer():   boolean { return this.selectedPartyType?.id === 3; }
    get isGovernment(): boolean { return this.selectedPartyType?.id === 4; }

    // ── Contractors (supplier browse) ────────────────────────────────────────────

    async browseSupplier(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseSupplierModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                const supplier = result.data;
                const duplicate = this.contractors.some(c => c.accountNo === supplier.accountNo);
                if (duplicate) {
                    this.alertService.warning(this.module, 'Duplicate', 'This supplier has already been added.');
                    return;
                }
                this.contractors.push({ accountNo: supplier.accountNo || '', name: supplier.name || '' });
            }
        } catch { }
    }

    removeContractor(index: number): void {
        this.contractors.splice(index, 1);
    }

    // ── As Planned Staking Sheet ─────────────────────────────────────────────────

    openStakingFilePicker(): void {
        this.stakingFileInput.nativeElement.click();
    }

    onStakingFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files) {
            const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
            Array.from(input.files).forEach(f => {
                if (allowed.includes(f.type)) this.stagedStakingFiles.push(f);
            });
        }
        input.value = '';
    }

    removeStagedFile(index: number): void {
        this.stagedStakingFiles.splice(index, 1);
    }

    isImage(file: File): boolean {
        return file.type.startsWith('image/');
    }

    fileUrl(fileId: number): string {
        return this.service.fileUrl(fileId);
    }

    // ── Save ─────────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.name) {
            this.alertService.warning(this.module, 'Validation', 'Project Name is required.');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            name:           this.name,
            location:       this.location       || null,
            purpose:        this.purpose        || null,
            paymentDetails: this.paymentDetails || null,
            projectManager: this.projectManager || null,
            contractors:    this.contractors.map(c => ({ supplier: { accountNo: c.accountNo, name: c.name } })),
            requestingPartyType: this.selectedPartyType?.description || null,
        };

        if (this.isOffice) {
            payload.office              = this.selectedOffice ? { id: this.selectedOffice.id } : null;
            payload.requestingPartyName = this.selectedOffice?.name || null;
        } else if (this.isDepartment) {
            payload.department          = this.selectedDepartment ? { id: this.selectedDepartment.id } : null;
            payload.requestingPartyName = this.selectedDepartment?.name || null;
        } else if (this.isConsumer) {
            payload.consumerAccountNumber = this.consumerAccountNumber || null;
            payload.consumerName          = this.consumerName          || null;
            payload.requestingPartyName   = this.consumerName          || null;
        } else if (this.isGovernment) {
            payload.governmentOfficeName = this.governmentOfficeName || null;
            payload.requestingPartyName  = this.governmentOfficeName || null;
        }

        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode ? this.service.update(payload) : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                if (data?.success === false) {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                    return;
                }
                const savedId = data?.modelId ?? data?.id ?? this.id;
                if (this.stagedStakingFiles.length > 0) {
                    this.uploadAndNavigate(savedId);
                } else {
                    this.formSubmit = false;
                    this.alertService.success(this.module, this.editMode ? 'Updated' : 'Created', '');
                    this.router.navigate(['/' + this.menuLink, savedId, 'detail']);
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'Save', '');
            }
        });
    }

    private uploadAndNavigate(savedId: number): void {
        const formData = new FormData();
        this.stagedStakingFiles.forEach(f => formData.append('files', f, f.name));
        this.service.uploadFiles(savedId, formData).subscribe({
            next: () => {
                this.formSubmit = false;
                this.alertService.success(this.module, this.editMode ? 'Updated' : 'Created', '');
                this.router.navigate(['/' + this.menuLink, savedId, 'detail']);
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.success(this.module, this.editMode ? 'Updated' : 'Created', 'Record saved but file upload failed.');
                this.router.navigate(['/' + this.menuLink, savedId, 'detail']);
            }
        });
    }
}
