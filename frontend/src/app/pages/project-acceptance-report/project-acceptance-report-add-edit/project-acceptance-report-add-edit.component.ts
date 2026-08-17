import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { ProjectAcceptanceReportService } from '../project-acceptance-report.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseProjectModalComponent } from '@/app/shared/modals/browse-project-modal/browse-project-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';

@Component({
    selector: 'app-project-acceptance-report-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './project-acceptance-report-add-edit.component.html'
})
export class ProjectAcceptanceReportAddEditComponent {
    module    = 'Project Acceptance Report';
    subModule = 'Create';
    menuLink  = 'project-acceptance-report';

    id: any    = null;
    editMode   = false;
    isLoading  = signal(false);

    // Browse fields
    project:       any = null;
    inspector1:    any = null;
    inspector2:    any = null;
    inspector3:    any = null;
    notedBy:       any = null;
    recommendedBy: any = null;
    approvedBy:    any = null;

    private service      = inject(ProjectAcceptanceReportService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(ModalService);

    ngOnInit(): void {
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

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.project       = data.project       || null;
                    this.inspector1    = data.inspector1    || null;
                    this.inspector2    = data.inspector2    || null;
                    this.inspector3    = data.inspector3    || null;
                    this.notedBy       = data.notedBy       || null;
                    this.recommendedBy = data.recommendedBy || null;
                    this.approvedBy    = data.approvedBy    || null;
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

    async browseProject(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseProjectModalComponent,
                {},
                { size: 'xl', centered: true }
            );
            if (result?.action === 'select') {
                this.project = result.data;
            }
        } catch (_) {}
    }

    async browseInspector(slot: 1 | 2 | 3): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent,
                { entityTypes: [1] },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select') {
                if (slot === 1) this.inspector1 = result.data;
                else if (slot === 2) this.inspector2 = result.data;
                else this.inspector3 = result.data;
            }
        } catch (_) {}
    }

    async browseSignatory(field: 'notedBy' | 'recommendedBy' | 'approvedBy'): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                BrowseEntityModalComponent,
                { entityTypes: [1] },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select') {
                this[field] = result.data;
            }
        } catch (_) {}
    }

    save(): void {
        if (!this.project) {
            this.alertService.warning(this.module, 'Validation', 'Project is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            project:       { id: this.project.id },
            inspector1:    this.inspector1    ? { id: this.inspector1.id }    : null,
            inspector2:    this.inspector2    ? { id: this.inspector2.id }    : null,
            inspector3:    this.inspector3    ? { id: this.inspector3.id }    : null,
            notedBy:       this.notedBy       ? { id: this.notedBy.id }       : null,
            recommendedBy: this.recommendedBy ? { id: this.recommendedBy.id } : null,
            approvedBy:    this.approvedBy    ? { id: this.approvedBy.id }    : null
        };

        if (this.editMode) payload.id = this.id;

        const request$ = this.editMode
            ? this.service.update(payload)
            : this.service.create(payload);

        request$.subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.success === false) {
                    this.alertService.error(this.module, 'Save', data.failureMessage || '');
                } else {
                    this.alertService.success(this.module, this.editMode ? 'Updated' : 'Created', '');
                    const id = data?.modelId ?? data?.id ?? this.id;
                    this.router.navigate(['/' + this.menuLink, id, 'detail']);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Save', '');
            }
        });
    }
}
