import { Component, ChangeDetectionStrategy, ChangeDetectorRef, inject } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { MemorandumReceiptService } from '@/app/pages/memorandum-receipt/memorandum-receipt.service';

@Component({
    selector: 'app-browse-returned-mr-modal',
    templateUrl: './browse-returned-mr-modal.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS]
})
export class BrowseReturnedMrModalComponent {
    employeeAccountNo: number = 0;
    records:  any[]  = [];
    isLoading        = false;
    page      = 1;
    pageSize  = 10;

    get total(): number { return this.records.length; }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.records.slice(start, start + this.pageSize);
    }

    private activeModal = inject(NgbActiveModal);
    private service     = inject(MemorandumReceiptService);
    private cdr         = inject(ChangeDetectorRef);

    ngOnInit(): void { this.load(); }

    load(): void {
        this.isLoading = true;
        this.service.getReturnedMrsByEmployee(this.employeeAccountNo).subscribe({
            next: (data) => { this.records = data || []; this.isLoading = false; this.cdr.markForCheck(); },
            error: () => { this.isLoading = false; this.cdr.markForCheck(); }
        });
    }

    select(doc: any): void { this.activeModal.close({ action: 'select', data: doc }); }
    dismiss(): void { this.activeModal.dismiss(); }
}
