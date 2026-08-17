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
import { CheckReleasingService } from '../check-releasing.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { TakePictureModalComponent } from '@/app/shared/modals/take-picture-modal/take-picture-modal.component';

@Component({
    selector: 'app-check-releasing-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './check-releasing-add-edit.component.html'
})
export class CheckReleasingAddEditComponent {
    module    = 'Check Releasing';
    subModule = 'Release Check';
    menuLink  = 'check-releasing';

    @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

    checkId: any  = null;
    checkInfo: any = null;
    formSubmit    = false;
    submit        = false;
    isLoading     = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // Form fields
    receivedBy   = '';
    idNumber     = '';
    orNumber     = '';
    depositSlip  = '';
    dateReleased = '';
    remarks      = '';

    // Image capture (base64)
    personImage: string | null = null;

    // File attachments [{ file: File, name: string, url: string }]
    attachments: { file: File; name: string; url: string }[] = [];

    private service      = inject(CheckReleasingService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.checkId  = this.route.snapshot.params['id'];
        const nav     = this.router.getCurrentNavigation();
        this.checkInfo    = nav?.extras?.state?.['check'] || null;
        this.dateReleased = this.toDateString(new Date());
        this.receivedBy   = this.checkInfo?.payee || '';
    }

    toDateString(d: Date): string {
        return d.toISOString().substring(0, 10);
    }

    // ─── Image capture ──────────────────────────────────────────────

    async takePicture(): Promise<void> {
        try {
            const result = await this.modalService.openModal(
                TakePictureModalComponent,
                { title: 'Capture Recipient Photo', message: '' },
                { size: 'lg', centered: true }
            );
            if (result?.action === 'select' && result?.data) {
                this.personImage = result.data;
            }
        } catch { }
    }

    retakePicture(): void {
        this.personImage = null;
        this.takePicture();
    }

    removePicture(): void {
        this.personImage = null;
    }

    // ─── File attachments ───────────────────────────────────────────

    openFilePicker(): void {
        this.fileInputRef?.nativeElement.click();
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files?.length) return;

        for (const file of Array.from(input.files)) {
            const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
            if (!allowed.includes(file.type)) {
                this.alertService.warning(this.module, 'Invalid file type', `${file.name} must be JPG, PNG, or PDF.`);
                continue;
            }
            this.attachments.push({
                file,
                name: file.name,
                url:  URL.createObjectURL(file)
            });
        }
        // Reset so the same file can be re-selected
        input.value = '';
    }

    removeAttachment(index: number): void {
        URL.revokeObjectURL(this.attachments[index].url);
        this.attachments.splice(index, 1);
    }

    isImage(attachment: { name: string }): boolean {
        return /\.(jpg|jpeg|png)$/i.test(attachment.name);
    }

    // ─── Form validation & submit ───────────────────────────────────

    isValid(): boolean {
        return !!(this.receivedBy?.trim() && this.dateReleased);
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) { return; }

        this.formSubmit = true;

        const payload = {
            check:        { id: this.checkId },
            receivedBy:   this.receivedBy.trim(),
            idNumber:     this.idNumber?.trim()    || null,
            orNumber:     this.orNumber?.trim()    || null,
            depositSlip:  this.depositSlip?.trim() || null,
            dateReleased: this.dateReleased,
            remarks:      this.remarks?.trim()     || null,
            personImage:  this.personImage         || null
        };

        const formData = new FormData();
        formData.append('model',
            new Blob([JSON.stringify(payload)], { type: 'application/json' })
        );
        formData.append('filesToRemove',
            new Blob([JSON.stringify([])], { type: 'application/json' })
        );
        this.attachments.forEach((a, i) => {
            formData.append(`file_${i}`, a.file, a.name);
        });

        this.service.releaseWithFiles(formData).subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Check Released', '');
                    this.router.navigate(['/' + this.menuLink]);
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Release', res.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'Release', '');
            }
        });
    }

    cancel(): void {
        this.router.navigate(['/' + this.menuLink]);
    }
}
