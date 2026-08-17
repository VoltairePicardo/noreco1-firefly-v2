import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { LaddaModule } from 'angular2-ladda';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BrowseSupplierModalComponent } from '@/app/shared/modals/browse-supplier-modal/browse-supplier-modal.component';
import { SubSupplierService } from '../sub-supplier.service';

@Component({
    selector: 'app-sub-supplier-upload',
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule, LaddaModule],
    templateUrl: './sub-supplier-upload.component.html'
})
export class SubSupplierUploadComponent {
    module    = 'Sub-Supplier';
    subModule = 'Upload';
    menuLink  = 'sub-supplier';

    selectedSupplier: any = null;
    selectedFile: File | null = null;
    isSubmitting = signal(false);
    submit       = false;

    private service      = inject(SubSupplierService);
    private alertService = inject(AlertService);
    private router       = inject(Router);
    private modalService = inject(NgbModal);

    openSupplierBrowse(): void {
        const ref = this.modalService.open(BrowseSupplierModalComponent, { size: 'lg', centered: true });
        ref.result.then((result) => {
            if (result?.action === 'select') {
                this.selectedSupplier = result.data;
            }
        }).catch(() => {});
    }

    clearSupplier(): void {
        this.selectedSupplier = null;
    }

    onFileChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        this.selectedFile = input.files?.[0] ?? null;
    }

    processUpload(): void {
        this.submit = true;
        if (!this.selectedSupplier || !this.selectedFile) { return; }

        this.isSubmitting.set(true);
        this.service.upload(this.selectedFile, this.selectedSupplier.id).subscribe({
            next: (res) => {
                this.isSubmitting.set(false);
                if (res?.success) {
                    this.alertService.success(this.module, 'Upload', res.successMessage ?? 'Upload successful!');
                    this.router.navigate(['/' + this.menuLink]);
                } else {
                    this.alertService.error(this.module, 'Upload', res?.failureMessage ?? '');
                }
            },
            error: () => { this.isSubmitting.set(false); this.alertService.error(this.module, 'Upload', ''); }
        });
    }
}
