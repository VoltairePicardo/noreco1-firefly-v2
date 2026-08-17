import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDirective, provideFlatpickrDefaults } from 'angularx-flatpickr';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DiService, PRINT_BASE_MAP, RV_FOR_IT, RV_FOR_REP, RV_FOR_LABOR } from '../di.service';
import { DownloadService } from '@/app/services/download.service';
import { AlertService } from '@/app/shared/services/alert.service';
import { BrowseSupplierModalComponent } from '@/app/shared/modals/browse-supplier-modal/browse-supplier-modal.component';

// DocumentType IDs (from v2 DocumentType enum getId())
const APV_ID  = 4;
const CV_ID   = 5;
const JV_ID   = 6;
const CRV_ID  = 9;
const SV_ID   = 13;
const RV_ID   = 1;
const CF_ID   = 20;   // Canvass
const PO_ID   = 2;
const JO_ID   = 10;
const JOA_ID  = 21;
const PR_ID   = 22;
const QS_ID   = 32;   // Quotation Summary
const RR_ID   = 3;
const SW_ID   = 8;
const SRL_ID  = 11;
const ST_ID   = 35;
const SA_ID   = 18;
const MCT_ID  = 33;
const MST_ID  = 34;
const SRC_ID  = 36;
const CE_ID   = 40;
const SIR_ID  = 39;

@Component({
    selector: 'app-di-main',
    standalone: true,
    imports: [
        ...COMMON_ALL_PAGE_IMPORTS,
        ...COMMON_MAIN_PAGE_IMPORTS,
        FlatpickrDirective,
    ],
    providers: [...SHARED_PROVIDERS, provideFlatpickrDefaults()],
    templateUrl: './di-main.component.html',
})
export class DiMainComponent {
    module   = 'Document Inquiry';
    menuLink = 'di';

    private svc          = inject(DiService);
    private modalService = inject(NgbModal);
    private downloadSvc  = inject(DownloadService);
    private alertService = inject(AlertService);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    // ── Primary filters ────────────────────────────────────────────────────
    fromDate            = '';
    toDate              = '';
    selectedDepartment: any = null;
    selectedDocType:    any = null;

    // ── Supplier filter (APV / CV) ─────────────────────────────────────────
    selectedSupplier:   any = null;
    dueDate             = '';

    // ── Account/amount filters (APV, CV, JV, CRV, SV) ─────────────────────
    codeFilter          = '';
    amountEntryFilter   = '';
    totalsFilter        = '';

    // ── Toolbar state ──────────────────────────────────────────────────────
    voucherQuery        = '';
    selectedUser:       any = null;
    filterStatus        = false;
    filterDate          = false;
    filterDocNo         = false;
    filterParticulars   = false;
    showUserFilter      = false;

    // ── Doc type flags (set on selectDocument) ─────────────────────────────
    forPurchasing       = true;
    forInventory        = false;
    rv                  = true;     // show Amount col in purchasing table (false for RV, Canvass)
    isReceivingReport   = false;
    isWithdrawal        = false;
    isReleasing         = false;
    isStockTransfer     = false;
    isReceiving         = false;
    isStockAdjustment   = false;
    isMCT               = false;
    isMST               = false;
    showQuotationDetails        = false;
    showCostEstimateDetails     = false;
    showSiteInspectionDetails   = false;
    showInventoryDetailAmount   = true;

    // ── Signals ────────────────────────────────────────────────────────────
    docTypes              = signal<any[]>([]);
    departments           = signal<any[]>([]);
    users                 = signal<any[]>([]);
    docList               = signal<any[]>([]);
    selectedDoc           = signal<any>(null);
    docInqDetails         = signal<any[]>([]);
    cycles                = signal<any[]>([]);
    quotationDetails      = signal<any[]>([]);
    quotationSuppliers    = signal<any[]>([]);
    costEstimateDetails   = signal<any[]>([]);
    siteInspectionDetails = signal<any[]>([]);
    logs                  = signal<any[]>([]);
    isLoading             = signal(false);
    showFilters           = signal(false);
    showPrint             = signal(false);
    showLogs              = signal(false);

    // ── Computed visibility ────────────────────────────────────────────────
    get showAccountFilters(): boolean {
        return [APV_ID, CV_ID, JV_ID, CRV_ID, SV_ID].includes(this.selectedDocType?.id);
    }
    get showSupplierFilter(): boolean {
        return [APV_ID, CV_ID].includes(this.selectedDocType?.id);
    }
    get showDueDateFilter(): boolean {
        return this.selectedDocType?.id === APV_ID;
    }

    // ── Grouped doc types for <optgroup> rendering ─────────────────────────
    get docTypeGroups(): { module: string; types: any[] }[] {
        const map = new Map<string, any[]>();
        for (const dt of this.docTypes()) {
            if (!map.has(dt.module)) map.set(dt.module, []);
            map.get(dt.module)!.push(dt);
        }
        // Sort groups by order field of first item in group
        return [...map.entries()]
            .sort((a, b) => (a[1][0]?.order ?? 0) - (b[1][0]?.order ?? 0))
            .map(([module, types]) => ({ module, types }));
    }

    // ── Filtered doc list (client-side column filters) ─────────────────────
    get filteredDocList(): any[] {
        let rows = this.docList();
        if (this.filterStatus && this._statusFilter)
            rows = rows.filter(r => r.status?.toLowerCase().includes(this._statusFilter.toLowerCase()));
        if (this.filterDate && this._dateFilter)
            rows = rows.filter(r => (r.voucherDate ?? '').toString().includes(this._dateFilter));
        if (this.filterDocNo && this._docNoFilter)
            rows = rows.filter(r => r.localCode?.toLowerCase().includes(this._docNoFilter.toLowerCase()));
        if (this.filterParticulars && this._particularsFilter)
            rows = rows.filter(r => r.particulars?.toLowerCase().includes(this._particularsFilter.toLowerCase()));
        return rows;
    }
    _statusFilter      = '';
    _dateFilter        = '';
    _docNoFilter       = '';
    _particularsFilter = '';

    // ── Lifecycle ──────────────────────────────────────────────────────────
    ngOnInit(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = now.toISOString().substring(0, 10);

        this.svc.getDocumentTypes().subscribe({ next: data => this.docTypes.set(data) });
        this.svc.getDepartments().subscribe({ next: data => this.departments.set(data) });
        this.svc.getUsers().subscribe({ next: data => this.users.set(data) });
        this.svc.getUserDefaultDepartment().subscribe({
            next: data => {
                if (Array.isArray(data) && data.length > 0) this.selectedDepartment = data[0];
                else if (data?.id) this.selectedDepartment = data;
            }
        });
    }

    // ── Doc type change ────────────────────────────────────────────────────
    onDocTypeChange(): void {
        this.docList.set([]);
        this.selectedDoc.set(null);
        this.docInqDetails.set([]);
        this.cycles.set([]);
        this.logs.set([]);
        this.showFilters.set(false);
        this.showPrint.set(false);
        this.showLogs.set(false);
        this.resetDetailFlags();
    }

    private resetDetailFlags(): void {
        this.forPurchasing = true;
        this.forInventory = false;
        this.rv = true;
        this.isReceivingReport = false;
        this.isWithdrawal = false;
        this.isReleasing = false;
        this.isStockTransfer = false;
        this.isReceiving = false;
        this.isStockAdjustment = false;
        this.isMCT = false;
        this.isMST = false;
        this.showQuotationDetails = false;
        this.showCostEstimateDetails = false;
        this.showSiteInspectionDetails = false;
        this.quotationDetails.set([]);
        this.quotationSuppliers.set([]);
        this.costEstimateDetails.set([]);
        this.siteInspectionDetails.set([]);
    }

    // ── Search ─────────────────────────────────────────────────────────────
    search(): void {
        if (!this.selectedDocType) {
            this.alertService.error(this.module, 'Validation', 'Please select a document type.');
            return;
        }
        const deptId = this.selectedDepartment?.id ?? 0;
        const suppId = this.selectedSupplier?.id ?? 0;
        const dDate  = this.showDueDateFilter ? this.dueDate : '';
        const c      = this.showAccountFilters ? this.codeFilter : '';
        const eAmt   = this.showAccountFilters ? this.amountEntryFilter : '';
        const tAmt   = this.showAccountFilters ? this.totalsFilter : '';

        this.isLoading.set(true);
        this.docList.set([]);
        this.selectedDoc.set(null);
        this.docInqDetails.set([]);
        this.cycles.set([]);

        // Set forPurchasing / forInventory / rv flags from docType config
        const typeConfig = this.selectedDocType;
        if (typeConfig.type === 1) {
            this.forPurchasing = true;
            this.forInventory  = false;
            // rv=false hides Amount column for RV and Canvass (no line amounts at header level)
            this.rv = typeConfig.id !== RV_ID && typeConfig.id !== CF_ID;
        } else if (typeConfig.type === 2) {
            this.forPurchasing = false;
            this.forInventory  = false;
        } else {
            // type 3 (inventory) and type 4 (work order) → forInventory=true
            this.forPurchasing = false;
            this.forInventory  = true;
        }

        this.svc.getDocuments(
            typeConfig.id, typeConfig.tableName, this.fromDate, this.toDate, typeConfig.pt,
            suppId, dDate, c, eAmt, tAmt, deptId
        ).subscribe({
            next: data => { this.docList.set(data ?? []); this.isLoading.set(false); this.showFilters.set(true); },
            error: ()  => { this.alertService.error(this.module, 'Load', 'Failed to load documents.'); this.isLoading.set(false); }
        });
    }

    searchByQuery(): void {
        if (!this.selectedDocType) {
            this.alertService.error(this.module, 'Validation', 'Please select a document type first.');
            return;
        }
        if (!this.voucherQuery.trim()) {
            this.alertService.warning(this.module, 'Validation', 'Search field is empty.');
            return;
        }
        this.isLoading.set(true);
        this.svc.searchDocuments(this.voucherQuery.trim()).subscribe({
            next: data => { this.docList.set(data ?? []); this.isLoading.set(false); this.showFilters.set(true); },
            error: ()  => { this.alertService.error(this.module, 'Load', 'Failed to search documents.'); this.isLoading.set(false); }
        });
    }

    filterByUser(): void {
        if (!this.selectedUser || !this.selectedDocType) return;
        this.isLoading.set(true);
        this.svc.getDocumentsByUser(
            this.selectedDocType.id, this.selectedDocType.tableName,
            this.fromDate, this.toDate, this.selectedDocType.pt,
            this.selectedUser.id
        ).subscribe({
            next: data => { this.docList.set(data ?? []); this.isLoading.set(false); },
            error: ()  => { this.alertService.error(this.module, 'Load', 'Failed to load documents.'); this.isLoading.set(false); }
        });
    }

    // ── Supplier browse ────────────────────────────────────────────────────
    browseSupplier(): void {
        const ref = this.modalService.open(BrowseSupplierModalComponent, { size: 'lg', centered: true });
        ref.result.then((supplier) => {
            if (supplier) this.selectedSupplier = supplier;
        }, () => {});
    }

    removeSupplier(): void {
        this.selectedSupplier = null;
    }

    // ── Row selection ──────────────────────────────────────────────────────
    selectDocument(doc: any): void {
        this.selectedDoc.set(doc);
        this.showPrint.set(true);
        this.docInqDetails.set([]);
        this.cycles.set([]);
        this.logs.set([]);
        this.showLogs.set(false);

        // Reset special-panel flags
        this.isReceivingReport = false;
        this.isWithdrawal      = false;
        this.isReleasing       = false;
        this.isStockTransfer   = false;
        this.isReceiving       = false;
        this.isStockAdjustment = false;
        this.isMCT             = false;
        this.isMST             = false;
        this.showQuotationDetails      = false;
        this.showCostEstimateDetails   = false;
        this.showSiteInspectionDetails = false;
        this.quotationDetails.set([]);
        this.quotationSuppliers.set([]);
        this.costEstimateDetails.set([]);
        this.siteInspectionDetails.set([]);

        const id      = doc.id;
        const transId = doc.transId;
        const typeId  = this.selectedDocType?.id;

        // Auto-load logs
        if (transId) this.loadLogs(transId);

        // Route to appropriate detail loader
        if (typeId === RV_ID) {
            this.svc.getRvDetails(id).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
        } else if (typeId === CF_ID) {
            this.svc.getCanvassDetails(id).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
        } else if (typeId === PO_ID) {
            this.svc.getPoDetails(id).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
        } else if (typeId === JO_ID) {
            this.svc.getJobOrderDetails(id).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
        } else if (typeId === JOA_ID) {
            this.svc.getJoaDetails(id).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
        } else if (typeId === PR_ID) {
            // PR uses fkId (JOA id) for details
            this.svc.getJoaDetails(doc.fkId).subscribe({ next: d => { this.setDetails(d); this.loadPurchaseCycles(this.docInqDetails()); } });
        } else if (typeId === QS_ID) {
            this.svc.getQuotationDetails(id).subscribe({
                next: d => {
                    this.setDetails(d);
                    this.loadQuotation(id);
                    this.loadPurchaseCycles(this.docInqDetails());
                }
            });
        } else if (typeId === RR_ID) {
            this.isReceivingReport = true;
            this.svc.getRrDetails(id).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
        } else if (typeId === SW_ID) {
            this.isWithdrawal = true;
            this.svc.getSwItems(id).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
        } else if (typeId === SRL_ID) {
            this.isReleasing = true;
            this.svc.getSrlItems(id).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
        } else if (typeId === ST_ID) {
            this.isStockTransfer = true;
            this.svc.getInvItems(transId).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
        } else if (typeId === SA_ID) {
            this.isStockAdjustment = true;
            this.svc.getInvItems(transId).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
        } else if (typeId === MCT_ID) {
            this.isMCT = true;
            this.svc.getInvItems(transId).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
        } else if (typeId === MST_ID) {
            this.isMST = true;
            this.svc.getInvItems(transId).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
        } else if (typeId === SRC_ID) {
            this.isReceiving = true;
            this.svc.getInvItems(transId).subscribe({ next: d => { this.setInventoryDetails(d); this.loadInventoryCycles(transId); } });
        } else if (typeId === CE_ID) {
            this.showCostEstimateDetails = true;
            this.loadAccountingCycles(transId);
            this.svc.getCeDetails(id).subscribe({ next: d => this.costEstimateDetails.set(d ?? []) });
        } else if (typeId === SIR_ID) {
            this.showSiteInspectionDetails = true;
            this.loadAccountingCycles(transId);
            this.svc.getSirDetails(id).subscribe({ next: d => this.siteInspectionDetails.set(d ?? []) });
        } else {
            // Accounting documents (APV, CV, JV, CRV, SV, AJ, etc.)
            this.loadAccountingCycles(transId);
            this.svc.getGlEntries(transId).subscribe({ next: d => this.setDetails(d) });
        }
    }

    // Exactly mirrors old JS setDetails() — field mapping preserved verbatim
    private setDetails(data: any[]): void {
        const typeId = this.selectedDocType?.id;
        this.docInqDetails.set((data ?? []).map(v => ({
            id: typeId === JOA_ID
                ? v.rvDetailId
                : v.rvDetailId != null ? v.rvDetailId : (v.id != null ? v.id : v.accountId),
            unit:        v.unitCode,
            particulars: v.itemDescription ?? v.joDescription ?? v.description,
            quantity:    v.quantity,
            amount:      v.itemAmount,
            debit:       v.debit,
            credit:      v.credit,
        })));
    }

    // Exactly mirrors old JS setInventoryDetails()
    private setInventoryDetails(data: any[]): void {
        const typeId = this.selectedDocType?.id;
        this.showInventoryDetailAmount = true;
        const details = (data ?? []).map(v => {
            let particulars: string;
            let amount: number | null = null;
            if (typeId === RR_ID) {
                particulars = v.description;
                amount      = v.netAmount;
            } else if (typeId === SW_ID) {
                particulars = v.itemDescription;
                this.showInventoryDetailAmount = false;
            } else if (typeId === SRL_ID) {
                particulars = v.itemDescription;
                this.showInventoryDetailAmount = false;
            } else {
                // ST, SA, MCT, MST, SRC
                particulars = v.itemDescription;
                amount      = v.totalCost;
                this.showInventoryDetailAmount = true;
            }
            return { id: v.id, unit: v.unitCode, particulars, quantity: v.quantity ?? v.quantityOrdered, amount };
        });
        this.docInqDetails.set(details);
    }

    private loadPurchaseCycles(details: any[]): void {
        details.forEach(d => {
            this.svc.getPurchaseCycle(d.id).subscribe({
                next: data => {
                    const newCycles = (data ?? []).map(v => ({
                        id: v.id, code: v.localCode, voucherDate: v.voucherDate, createdAt: v.createdAt
                    }));
                    this.cycles.update(c => {
                        const existing = new Set(c.map((x: any) => x.code));
                        return [...c, ...newCycles.filter(x => !existing.has(x.code))];
                    });
                }
            });
        });
    }

    private loadAccountingCycles(transId: number): void {
        this.svc.getAccountingCycle(transId).subscribe({
            next: data => this.cycles.set((data ?? []).map(v => ({
                id: v.id, code: v.localCode, voucherDate: v.voucherDate
            })))
        });
    }

    private loadInventoryCycles(transId: number): void {
        this.svc.getInventoryCycle(transId).subscribe({
            next: data => this.cycles.set((data ?? []).map(v => ({
                id: v.id, code: v.localCode, voucherDate: v.voucherDate
            })))
        });
    }

    private loadQuotation(quotationId: number): void {
        this.svc.getQuotationDetails(quotationId).subscribe({
            next: data => {
                if (!data || !data.length) return;
                this.quotationDetails.set(data);
                const first = data[0];
                if (first?.details) this.quotationSuppliers.set(first.details.map((d: any) => d.supplier));
                this.showQuotationDetails = true;
            }
        });
    }

    // ── Logs ───────────────────────────────────────────────────────────────
    toggleLogs(): void {
        if (this.showLogs()) { this.showLogs.set(false); return; }
        const transId = this.selectedDoc()?.transId;
        if (!transId) return;
        this.svc.getLogs(transId).subscribe({
            next: data => { this.logs.set(data ?? []); this.showLogs.set(true); },
            error: ()  => this.alertService.error(this.module, 'Load', 'Failed to load logs.')
        });
    }

    deserializeLog(json: string): any {
        try { return JSON.parse(json); } catch { return {}; }
    }

    private loadLogs(transId: number): void {
        this.svc.getLogs(transId).subscribe({
            next: data => this.logs.set(data ?? [])
        });
    }

    // ── Print ──────────────────────────────────────────────────────────────
    print(): void {
        const doc  = this.selectedDoc();
        const desc = this.selectedDocType?.desc;
        if (!doc || !desc) return;
        const base = PRINT_BASE_MAP[desc];
        if (!base) { this.alertService.warning(this.module, 'Print', 'Print not supported for this document type.'); return; }
        const id = doc.id;
        let path: string;
        if (desc !== 'Purchase or Work Request') {
            path = `/${base}/export/${id}`;
        } else {
            const rvTypeId = doc.rvTypeId;
            if (rvTypeId === RV_FOR_REP)       path = `/${base}/export2/${id}`;
            else if (rvTypeId === RV_FOR_LABOR) path = `/${base}/export3/${id}`;
            else if (rvTypeId === RV_FOR_IT)    path = `/${base}/export1/${id}`;
            else                                path = `/${base}/export/${id}`;
        }
        this.downloadSvc.print(path, { type: 'pdf' });
    }
}
