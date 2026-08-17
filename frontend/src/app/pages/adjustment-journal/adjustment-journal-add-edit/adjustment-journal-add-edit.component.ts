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
import { AdjustmentJournalService } from '../adjustment-journal.service';
import { ModalService } from '@/app/shared/modals/modal-service';
import { BrowseEntityModalComponent } from '@/app/shared/modals/browse-entity-modal/browse-entity-modal.component';
import { JournalEntriesFormComponent, JournalEntry } from '@/app/shared/forms/journal-entries-form/journal-entries-form.component';
import { provideIcons } from '@ng-icons/core';
import {
    tablerSearch, tablerX, tablerArrowLeft, tablerCheck,
    tablerTrash, tablerPaperclip, tablerPhoto, tablerFile
} from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-adjustment-journal-add-edit',
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_ADD_EDIT_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
        JournalEntriesFormComponent
    ],
    providers: [
        provideFlatpickrDefaults(),
        ...SHARED_PROVIDERS,
        provideIcons({
            tablerSearch, tablerX, tablerArrowLeft, tablerCheck,
            tablerTrash, tablerPaperclip, tablerPhoto, tablerFile
        })
    ],
    templateUrl: './adjustment-journal-add-edit.component.html'
})
export class AdjustmentJournalAddEditComponent {
    @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

    module    = 'Adjustment Journal';
    subModule = 'Create';
    menuLink  = 'adjustment-journal';

    id: any    = null;
    editMode   = false;
    formSubmit = false;
    isLoading  = signal(false);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    voucherDate     = '';
    explanation     = '';
    transactionType = 'Adjustment';

    readonly transactionTypes = ['Adjustment', 'Closing', 'Reopening'];

    journalEntries: JournalEntry[] = [];

    // Attachments
    stagedFiles: File[]  = [];
    uploadingFiles       = false;

    signatories: { [key: string]: any } = {
        checker:              null,
        recommendingApproval: null,
        approvingOfficer:     null
    };

    signatoryLabels: { [key: string]: string } = {
        checker:              'Checker',
        recommendingApproval: 'Recommending for Approval',
        approvingOfficer:     'Approving Officer'
    };

    signatoryKeys = ['checker', 'recommendingApproval', 'approvingOfficer'];

    private service      = inject(AdjustmentJournalService);
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
                this.journalEntries = [{ account: null, debit: null, credit: null }];
                this.loadDefaultSignatories();
            }
        });
    }

    loadForEdit(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.voucherDate     = data.voucherDate ? new Date(data.voucherDate).toISOString().substring(0, 10) : '';
                    this.explanation     = data.explanation || data.remarks || '';
                    this.transactionType = data.transactionType || 'Adjustment';
                    this.journalEntries  = (data.journalEntries || data.details || []).map((e: any): JournalEntry => ({
                        account:         e.account || (e.code ? { accountCode: e.code, accountTitle: e.description, id: e.accountId } : null),
                        debit:           Number(e.debit ?? e.debitAmount) || null,
                        credit:          Number(e.credit ?? e.creditAmount) || null,
                        applyAllocation: e.applyAllocation || false,
                        allocationPct:   e.allocationPct   ?? null,
                        applyWht:        e.applyWht        || false,
                        wht:             e.wht             || null
                    }));
                    if (this.journalEntries.length === 0) {
                        this.journalEntries = [{ account: null, debit: null, credit: null }];
                    }
                    this.signatories = {
                        checker:              data.checker              || null,
                        recommendingApproval: data.recommendingApproval || null,
                        approvingOfficer:     data.approvingOfficer     || null
                    };
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

    loadDefaultSignatories(): void {
        this.service.getDefaultSignatories().subscribe({
            next: (data) => {
                if (data) {
                    this.signatories = {
                        checker:              data.checker              || null,
                        recommendingApproval: data.recommendingApproval || null,
                        approvingOfficer:     data.approvingOfficer     || null
                    };
                }
            },
            error: () => {}
        });
    }

    get totalDebit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.debit) || 0), 0);
    }

    get totalCredit(): number {
        return this.journalEntries.reduce((sum, e) => sum + (Number(e.credit) || 0), 0);
    }

    get isBalanced(): boolean {
        return this.journalEntries.length > 0 && Math.abs(this.totalDebit - this.totalCredit) < 0.001;
    }

    // Entity browse (signatories)
    async openEntityBrowse(key: string): Promise<void> {
        try {
            const result = await this.modalService.openModal(BrowseEntityModalComponent, { entityTypes: [1] }, { size: 'lg', centered: true });
            if (result?.action === 'select' && result?.data) {
                this.signatories[key] = result.data;
            }
        } catch { }
    }

    clearSignatory(key: string): void {
        this.signatories[key] = null;
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
        if (!this.voucherDate) {
            this.alertService.warning(this.module, 'Validation', 'Please enter a voucher date.');
            return;
        }
        if (this.journalEntries.length === 0) {
            this.alertService.warning(this.module, 'Validation', 'Please add at least one journal entry.');
            return;
        }
        if (!this.isBalanced) {
            this.alertService.warning(this.module, 'Validation', 'Debit and credit totals must be equal.');
            return;
        }

        this.formSubmit = true;

        const sigRef = (key: string) => this.signatories[key]
            ? { accountNo: this.signatories[key].accountNo } : null;

        const payload: any = {
            id:              this.editMode ? this.id : null,
            voucherDate:     this.voucherDate,
            explanation:     this.explanation || null,
            transactionType: this.transactionType,
            generalLedgerLines: this.journalEntries.map(e => ({
                code:        e.account?.accountCode  || '',
                description: e.account?.accountTitle || e.account?.accountDescription || '',
                accountId:   e.account?.id           || null,
                debit:       Number(e.debit)         || 0,
                credit:      Number(e.credit)        || 0
            })),
            checker:             sigRef('checker'),
            recommendingOfficer: sigRef('recommendingApproval'),
            approvingOfficer:    sigRef('approvingOfficer')
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
            error: () => {
                this.formSubmit = false;
                this.alertService.error(this.module, 'An error occurred while saving.', '');
            }
        });
    }
}
