import { Component, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import {
    COMMON_ALL_PAGE_IMPORTS,
    COMMON_ADD_EDIT_PAGE_IMPORTS,
    COMMON_MAIN_PAGE_IMPORTS,
    SHARED_PROVIDERS
} from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { BankDepositService } from '../bank-deposit.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseBankAccountModalComponent } from '@/app/shared/modals/browse-bank-account-modal/browse-bank-account-modal.component';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch, tablerX, tablerArrowLeft, tablerCheck, tablerPaperclip, tablerPhoto, tablerFile, tablerTrash, tablerUpload } from '@ng-icons/tabler-icons';


@Component({
    selector: 'app-bank-deposit-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({ tablerSearch, tablerX, tablerArrowLeft, tablerCheck, tablerPaperclip, tablerPhoto, tablerFile, tablerTrash, tablerUpload })
    ],
    templateUrl: './bank-deposit-add-edit.component.html'
})
export class BankDepositAddEditComponent {
    @ViewChild('fileInput')       fileInput!: ElementRef<HTMLInputElement>;
    @ViewChild('uploadFileInput') uploadFileInput!: ElementRef<HTMLInputElement>;

    module    = 'Bank Deposit';
    subModule = 'Create';
    menuLink  = 'bank-deposit';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    depositDate    = '';
    collectionDate = '';
    postingDate    = '';
    referenceNumber = '';
    collector   = '';
    cashAmount  = 0;
    checkAmount = 0;

    bankAccount: any = null;

    stagedFiles  : File[] = [];
    uploadingFiles = false;

    // Upload Deposits (bulk import)
    uploadFile: File | null = null;
    uploading = false;
    uploadMessage = '';
    uploadSuccess: boolean | null = null;

    private service      = inject(BankDepositService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private modalService = inject(ModalService);
    private alertService = inject(AlertService);

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
                    this.depositDate     = data.depositDate     ? new Date(data.depositDate).toISOString().substring(0, 10)    : '';
                    this.collectionDate  = data.collectionDate  ? new Date(data.collectionDate).toISOString().substring(0, 10) : '';
                    this.postingDate     = data.postingDate     ? new Date(data.postingDate).toISOString().substring(0, 10)    : '';
                    this.referenceNumber = data.referenceNumber || '';
                    this.collector       = data.collector || '';
                    this.cashAmount      = data.cashAmount  || 0;
                    this.checkAmount     = data.checkAmount || 0;
                    this.bankAccount     = data.bankAccount || null;
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

    async openBankBrowse(): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseBankAccountModalComponent, {}, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.bankAccount = result.data;
            }
        } catch { }
    }

    get totalAmount(): number {
        return (Number(this.cashAmount) || 0) + (Number(this.checkAmount) || 0);
    }

    // Attachments
    openFilePicker(): void {
        this.fileInput.nativeElement.click();
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files) {
            const allowed = ['image/jpeg', 'image/png', 'application/pdf'];
            Array.from(input.files).forEach(f => {
                if (allowed.includes(f.type)) this.stagedFiles.push(f);
            });
        }
        input.value = '';
    }

    removeStagedFile(index: number): void {
        this.stagedFiles.splice(index, 1);
    }

    isImage(file: File): boolean {
        return file.type.startsWith('image/');
    }

    // Upload Deposits
    openUploadFilePicker(): void {
        this.uploadFileInput.nativeElement.click();
    }

    onUploadFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files?.length) {
            this.uploadFile = input.files[0];
        }
        input.value = '';
    }

    doUpload(): void {
        if (!this.uploadFile) {
            this.alertService.warning(this.module, 'Validation', 'Please select a file to upload.');
            return;
        }
        this.uploading = true;
        this.uploadMessage = '';
        const formData = new FormData();
        formData.append('file', this.uploadFile, this.uploadFile.name);
        this.service.uploadDeposits(formData).subscribe({
            next: (res) => {
                this.uploading = false;
                if (res?.success) {
                    this.uploadSuccess = true;
                    this.uploadMessage = res?.message || 'Upload successful.';
                    this.uploadFile = null;
                } else {
                    this.uploadSuccess = false;
                    this.uploadMessage = res?.failureMessage || 'Upload failed.';
                }
            },
            error: () => {
                this.uploading = false;
                this.uploadSuccess = false;
                this.uploadMessage = 'An error occurred during upload.';
            }
        });
    }

    private uploadAndNavigate(savedId: number): void {
        const formData = new FormData();
        this.stagedFiles.forEach(f => formData.append('files', f, f.name));
        this.service.uploadFiles(savedId, formData).subscribe({
            next: () => {
                this.formSubmit = false;
                this.alertService.success(this.module, 'Saved successfully.', '');
                this.router.navigate(['/' + this.menuLink, savedId, 'detail']);
            },
            error: () => {
                this.formSubmit = false;
                this.alertService.success(this.module, 'Saved successfully.', 'Record saved but file upload failed.');
                this.router.navigate(['/' + this.menuLink, savedId, 'detail']);
            }
        });
    }

    save(): void {
        if (!this.depositDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a deposit date.');
            return;
        }
        if (!this.bankAccount) {
            this.alertService.warning(this.module, 'Validation', 'Please select a bank account.');
            return;
        }

        this.formSubmit = true;

        const payload: any = {
            id:              this.editMode ? this.id : null,
            depositDate:     this.depositDate,
            collectionDate:  this.collectionDate || null,
            postingDate:     this.postingDate    || null,
            referenceNumber: this.referenceNumber,
            collector:       this.collector || '',
            cashAmount:      Number(this.cashAmount)  || 0,
            checkAmount:     Number(this.checkAmount) || 0,
            bankAccount:     { id: this.bankAccount.id }
        };

        const req$ = this.editMode ? this.service.update(payload) : this.service.create(payload);
        req$.subscribe({
            next: (res) => {
                if (res?.success) {
                    if (this.stagedFiles.length > 0) {
                        this.uploadAndNavigate(res.modelId);
                    } else {
                        this.formSubmit = false;
                        this.alertService.success(this.module, 'Saved successfully.', '');
                        this.router.navigate(['/' + this.menuLink, res.modelId, 'detail']);
                    }
                } else {
                    this.formSubmit = false;
                    this.alertService.error(this.module, 'Save failed.', res?.failureMessage || '');
                }
            },
            error: () => { this.formSubmit = false; this.alertService.error(this.module, 'An error occurred.', ''); }
        });
    }
}
