import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountingReportsService } from '../accounting-reports.service';

interface DocTypeOption {
    desc: string;
    id: number;
    tableName: string;
    pt: string;
}

@Component({
    selector: 'app-pending-voucher-list',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './pending-voucher-list.component.html'
})
export class PendingVoucherListComponent {
    module   = 'Pending Voucher List';
    menuLink = 'accounting-reports';

    docTypes: DocTypeOption[] = [
        { desc: 'APV',     id: 4,  tableName: 'AccountsPayableVoucher',   pt: 'particulars' },
        { desc: 'Canvass', id: 20, tableName: 'Canvass',                  pt: 'FK_vendorAccountNo' },
        { desc: 'CRV',     id: 9,  tableName: 'CashReceipts',             pt: 'particulars' },
        { desc: 'CV',      id: 5,  tableName: 'CheckVoucher',             pt: 'particulars' },
        { desc: 'JOA',     id: 21, tableName: 'JoAcceptance',             pt: 'FK_vendorAccountNo' },
        { desc: 'JO',      id: 10, tableName: 'JobOrder',                 pt: 'FK_vendorAccountNo' },
        { desc: 'JV',      id: 6,  tableName: 'JournalVoucher',           pt: 'explanation' },
        { desc: 'MIR',     id: 19, tableName: 'MaterialIssueRegister',    pt: 'particulars' },
        { desc: 'PO',      id: 2,  tableName: 'PurchaseOrder',            pt: 'FK_vendorAccountNo' },
        { desc: 'RV',      id: 1,  tableName: 'RequisitionVoucher',       pt: 'purpose' },
        { desc: 'SV',      id: 13, tableName: 'SalesVoucher',             pt: 'particulars' },
    ];

    selectedDocType: DocTypeOption = this.docTypes[0];
    documentStatuses = signal<any[]>([]);
    selectedStatusId = 0;

    rows      = signal<any[]>([]);
    isLoading = signal(false);

    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);
    private service      = inject(AccountingReportsService);

    ngOnInit(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.documentStatuses.set([{ id: 0, status: 'All' }, ...(data || [])])
        });
    }

    search(): void {
        const dt = this.selectedDocType;
        this.isLoading.set(true);
        this.rows.set([]);
        this.service.getPendingVoucherList(dt.id, dt.tableName, dt.pt, this.selectedStatusId).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.isLoading.set(false); }
        });
    }

    export(type: 'pdf' | 'xls'): void {
        const dt     = this.selectedDocType;
        const status = this.documentStatuses().find(s => s.id === this.selectedStatusId);
        this.downloadSvc.print(
            `/reports/export/pending-voucher-list/tn/${dt.tableName}/pt/${dt.pt}`,
            {
                type,
                docTypeId: dt.id,
                docType:   dt.desc,
                statusId:  this.selectedStatusId,
                status:    status?.status ?? 'All'
            }
        );
    }
}
