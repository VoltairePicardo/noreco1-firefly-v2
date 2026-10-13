import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE     = environment.get('baseApiUrl');  // http://localhost:8080/api
const BASE_URL = environment.get('baseUrl');      // http://localhost:8080 (for non-/api controllers)

// Print base URL map — paths relative to baseApiUrl (/api)
export const PRINT_BASE_MAP: Record<string, string> = {
    'Accounts Payable':         'accounts-payable',
    'Journal Voucher':          'journal-voucher',
    'Check Voucher':            'check-voucher',
    'Purchase or Work Request': 'purchase-request',
    'Canvass':                  'canvass-rv',
    'Purchase Order':           'purchase-order',
    'Job Order':                'job-order',
    'JO Acceptance':            'jo-acceptance',
    'Payment Request':          'payment-request',
    'Summary of Quotation':     'quotation',
    'Sales Voucher':            'sales-voucher',
    'Cash Receipts':            'cash-receipts',
    'Receiving Report':         'rr',
    'Stock Withdrawal':         'withdrawal',
    'Stock Release':            'stock-release',
    'Stock Adjustment':         'stock-adjustment',
    'Stock Transfer':           'stock-transfer',
    'Stock Receive':            'stock-receive',
    'Material Credit Ticket':   'inventory/mct-mgt',
    'Material Salvage Ticket':  'mst',
    'Site Inspection Report':   'site-inspection-report',
    'Cost Estimate':            'cost-estimate',
};

// RV print type constants — from old Firefly firefly.js
export const RV_FOR_IT    = 2;
export const RV_FOR_REP   = 3;
export const RV_FOR_LABOR = 4;

@Injectable({ providedIn: 'root' })
export class DiService {
    private http = inject(HttpClient);

    // ── Doc types + departments + users ──────────────────────────────────────
    getDocumentTypes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/document-types`);
    }
    getDepartments(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/json/departments`);
    }
    getUserDefaultDepartment(): Observable<any> {
        return this.http.get<any>(`${BASE}/json/departments-by-user`);
    }
    getUsers(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/user/list`);
    }

    // ── Document list ─────────────────────────────────────────────────────────
    getDocuments(
        typeId: number, tableName: string, startDate: string, endDate: string, pt: string,
        suppId = 0, dDate = '', c = '', eAmt = '', tAmt = '', d = 0
    ): Observable<any[]> {
        return this.http.get<any[]>(
            `${BASE}/document-inquiry/list/${typeId}/tn/${tableName}/sd/${startDate}/ed/${endDate}/pt/${pt}`,
            { params: { suppId, dDate, c, eAmt, tAmt, d } as any }
        );
    }
    getDocumentsByUser(
        typeId: number, tableName: string, startDate: string, endDate: string, pt: string, userId: number
    ): Observable<any[]> {
        return this.http.get<any[]>(
            `${BASE}/document-inquiry/list/${typeId}/tn/${tableName}/sd/${startDate}/ed/${endDate}/pt/${pt}/userId/${userId}`
        );
    }
    searchDocuments(query: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/search/${query}`);
    }

    // ── Cycles ────────────────────────────────────────────────────────────────
    getPurchaseCycle(rvdId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/purchase-cycle/${rvdId}`);
    }
    getAccountingCycle(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/accounting-cycle/${transId}`);
    }
    getInventoryCycle(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/inventory-cycle/${transId}`);
    }

    // ── Purchasing details ────────────────────────────────────────────────────
    getRvDetails(rvId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/rv-detail/rvd/${rvId}`);
    }
    getCanvassDetails(canvassId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/canvass-detail/cnvsd/${canvassId}`);
    }
    getPoDetails(poId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/po-detail/pod/${poId}`);
    }
    getJobOrderDetails(joId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/job-order/detail/${joId}`);
    }
    getJoaDetails(joaId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/jo-acceptance/detail/${joaId}`);
    }
    getQuotationDetails(quotationId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/quotation-detail/${quotationId}`);
    }

    // ── Accounting details ────────────────────────────────────────────────────
    getGlEntries(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/ledger/gl/${transId}`);
    }

    // ── Inventory details ─────────────────────────────────────────────────────
    getRrDetails(rrId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/rr-detail/${rrId}`);
    }
    getSwItems(swId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/sw-items/${swId}`);
    }
    getSrlItems(srlId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/srl-items/${srlId}`);
    }
    // ST, SA, MCT, MST, SRC all use this — pass docInq.transId
    getInvItems(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/inv-items/${transId}`);
    }

    // ── Work order details ────────────────────────────────────────────────────
    getCeDetails(ceId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/ce-details/${ceId}`);
    }
    getSirDetails(sirId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/document-inquiry/sir-details/${sirId}`);
    }

    // ── Logs ──────────────────────────────────────────────────────────────────
    getLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE}/json/document-logs/${transId}`);
    }
}
