import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { IemopBillingService } from '../iemop-billing.service';

@Component({
    selector: 'app-iemop-billing-upload',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [provideFlatpickrDefaults(), ...SHARED_PROVIDERS],
    templateUrl: './iemop-billing-upload.component.html'
})
export class IemopBillingUploadComponent {
    module    = 'Upload IEMOP Billing';
    subModule = 'Upload';
    menuLink  = 'iemop-billing';

    submit     = false;
    formSubmit = false;
    isLoading  = signal(false);

    referenceNumber = '';
    transDate       = new Date().toISOString().substring(0, 10);
    selectedFile: File | null = null;
    fileName = '';

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service      = inject(IemopBillingService);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            this.selectedFile = input.files[0];
            this.fileName     = this.selectedFile.name;
        }
    }

    isValid(): boolean {
        return !!(this.referenceNumber?.trim() && this.transDate && this.selectedFile);
    }

    save(): void {
        this.submit = true;
        if (!this.isValid()) return;

        this.formSubmit = true;
        this.service.upload(this.selectedFile!, this.transDate, this.referenceNumber.trim()).subscribe({
            next: (res) => {
                this.formSubmit = false;
                if (res?.success) {
                    this.alertService.success(this.module, 'Upload completed successfully.', '');
                    this.router.navigate(['/' + this.menuLink]);
                } else {
                    this.alertService.error(this.module, 'Upload failed.', res?.failureMessage || '');
                }
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred during upload.', '');
            }
        });
    }

    reset(): void {
        this.referenceNumber = '';
        this.transDate       = new Date().toISOString().substring(0, 10);
        this.selectedFile    = null;
        this.fileName        = '';
        this.submit          = false;
    }

    cancel(): void {
        this.router.navigate(['/' + this.menuLink]);
    }
}
