import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { SiteInspectionReportService } from '../site-inspection-report.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseProjectModalComponent } from '@/app/shared/modals/browse-project-modal/browse-project-modal.component';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';

@Component({
    selector: 'app-site-inspection-report-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './site-inspection-report-add-edit.component.html'
})
export class SiteInspectionReportAddEditComponent {
    module    = 'Site Inspection Report';
    subModule = 'Create';
    menuLink  = 'site-inspection-report';

    id: any    = null;
    editMode   = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Form fields
    date = '';

    // Browse fields
    project:       any = null;
    concurredBy:   any = null;
    recommendedBy: any = null;
    approvedBy:    any = null;

    // Findings rows
    findings: { description: string; remarks: string }[] = [];

    // File attachments (staged for upload)
    @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;
    attachments: { file: File; name: string; url: string }[] = [];

    private service      = inject(SiteInspectionReportService);
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
                this.addFinding();
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.date          = data.date        ? new Date(data.date).toISOString().substring(0, 10) : '';
                    this.project       = data.project       || null;
                    this.concurredBy   = data.checker       || null;
                    this.recommendedBy = data.notedBy       || null;
                    this.approvedBy    = data.approvedBy    || null;
                    this.findings      = (data.findings || []).map((f: any) => ({
                        description: f.description || '',
                        remarks:     f.remarks     || ''
                    }));
                    if (this.findings.length === 0) this.addFinding();
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

    async browseSignatory(field: 'concurredBy' | 'recommendedBy' | 'approvedBy'): Promise<void> {
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

    addFinding(): void { this.findings.push({ description: '', remarks: '' }); }
    removeFinding(i: number): void { this.findings.splice(i, 1); }

    // ─── File Attachments ────────────────────────────────────────────────────

    openFilePicker(): void { this.fileInputRef?.nativeElement.click(); }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files?.length) return;
        for (const file of Array.from(input.files)) {
            if (!/\.(jpg|jpeg|png|pdf)$/i.test(file.name)) {
                this.alertService.warning(this.module, 'Invalid file type', `${file.name} is not allowed. Only JPG, PNG, PDF.`);
                continue;
            }
            this.attachments.push({ file, name: file.name, url: URL.createObjectURL(file) });
        }
        input.value = '';
    }

    removeAttachment(index: number): void {
        URL.revokeObjectURL(this.attachments[index].url);
        this.attachments.splice(index, 1);
    }

    isImage(att: { name: string }): boolean {
        return /\.(jpg|jpeg|png)$/i.test(att.name);
    }

    private uploadStagedFiles(id: number): void {
        if (!this.attachments.length) return;
        const formData = new FormData();
        this.attachments.forEach(a => formData.append(`file_${a.name}`, a.file, a.name));
        this.service.uploadFiles(id, formData).subscribe({ error: () => {} });
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    save(): void {
        if (!this.date) {
            this.alertService.warning(this.module, 'Validation', 'Date is required.');
            return;
        }

        this.isLoading.set(true);

        const payload: any = {
            date:       this.date,
            project:    this.project       ? { id: this.project.id }       : null,
            checker:    this.concurredBy   ? { id: this.concurredBy.id }   : null,
            notedBy:    this.recommendedBy ? { id: this.recommendedBy.id } : null,
            approvedBy: this.approvedBy    ? { id: this.approvedBy.id }    : null,
            findings:   this.findings.filter(f => f.description?.trim())
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
                    const id = data?.modelId ?? data?.id ?? this.id;
                    this.uploadStagedFiles(id);
                    this.alertService.success(this.module, this.editMode ? 'Updated' : 'Created', '');
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
