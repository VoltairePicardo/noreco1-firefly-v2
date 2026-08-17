import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class AccountingReportsService {
    private http = inject(HttpClient);

    getOffices(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/offices`);
    }

    getPcfList(): Observable<any> {
        return this.http.get<any>(`${BASE_API}/petty-cash-fund/list`, {
            params: new HttpParams().set('page', 0).set('size', 100)
        });
    }

    getPcvBatches(from: string, to: string, officeId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/petty-cash-voucher/batches`, {
            params: new HttpParams().set('from', from).set('to', to).set('officeId', officeId)
        });
    }

    getAssetLinkTypes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cpr/link-types`);
    }

    getMaintenanceRecords(q: string): Observable<any> {
        return this.http.get<any>(`${BASE_API}/maintenance-record/list`, {
            params: new HttpParams().set('q', q).set('page', 0).set('size', 20)
        });
    }

    getAssetLedgerData(from: string, to: string, accountId: number, assetAccountNo: number, linkTypeId: number): Observable<any[]> {
        return this.http.get<any[]>(
            `${BASE_API}/reports/accounting/asset-ledger/${from}/${to}/${accountId}/${assetAccountNo}/${linkTypeId}`
        );
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-statuses`);
    }

    getDepreciationSummary(year: number, month: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/depreciation-summary/${year}/${month}`);
    }

    getDepreciationSchedule(year: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/depreciation-schedule/${year}`);
    }

    getAccountsPayableAging(cutOff: string): Observable<any[]> {
        const params = new HttpParams().set('q', cutOff);
        return this.http.get<any[]>(`${BASE_API}/reports/data/accounts-payable-aging`, { params });
    }

    getCheckList(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/check-list/${from}/${to}`);
    }

    getCashFlowDetail(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/cash-flow-detail/${from}/${to}`);
    }

    getWorkInProgress(from: string, to: string, status: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/work-in-progress/${from}/${to}/${status}`);
    }

    getWorkOrderAging(asOf: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/work-order/${asOf}`);
    }

    getWorkOrderTransaction(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/work-order-transaction/${from}/${to}`);
    }

    getMaintenanceRecordSummary(from: string, to: string, q: string = ''): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/maintenance-record-summary/${from}/${to}`, { params: { q } });
    }

    getPendingPurchaseRequests(from: string, to: string, status: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/pending-purchase-requests/${from}/${to}/${status}`);
    }

    getUnliquidatedCA(status: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/unliquidated-ca/${status}`);
    }

    getBirForm1601E(year: number, month: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/form-1601E/${year}/${month}`);
    }

    getBirAlphalistData(year: number, month: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/bir-alphalist/${year}/${month}`);
    }

    getCashFlowStatement(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cashflow-item/statement/${from}/${to}`);
    }

    getCashFlowStatementBsup(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cashflow-item/statement-bsup/${from}/${to}`);
    }

    getTrialBalanceData(asOf: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/trial-balance`, {
            params: new HttpParams().set('q', asOf)
        });
    }

    getTransactionSummaryData(from: string, to: string): Observable<any> {
        return this.http.get<any>(`${BASE_API}/reports/data/transaction-summary-per-account`, {
            params: new HttpParams().set('s', from).set('e', to)
        });
    }

    getBalanceSheetData(asOf: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/balance-sheet`, {
            params: new HttpParams().set('q', asOf)
        });
    }

    getIncomeStatementData(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/income-statement`, {
            params: new HttpParams().set('q', from).set('r', to)
        });
    }

    getIncomeStatementNeaData(start: string, end: string, fsType: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/income-statement-nea`, {
            params: new HttpParams().set('start', start).set('end', end).set('fsType', fsType)
        });
    }

    getBalanceSheetNeaData(asOfDate: string, fsType: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/balance-sheet-nea`, {
            params: new HttpParams().set('q', asOfDate).set('fsType', fsType)
        });
    }

    getTrialBalanceNeaAuditedData(cutOffDate: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/trial-balance-nea-audited`, {
            params: new HttpParams().set('cutOffDate', cutOffDate)
        });
    }

    getTrialBalanceNeaData(start: string, end: string, fsType: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/trial-balance-nea`, {
            params: new HttpParams().set('start', start).set('end', end).set('fsType', fsType)
        });
    }

    getOpenMonthsForTrialBalance(start: string, end: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/open-months-for-trial-balance`, {
            params: new HttpParams().set('start', start).set('end', end)
        });
    }

    getJvRegister(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/jv-register/${from}/${to}/${statusId}`);
    }

    getCvRegister(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/cv-register/${from}/${to}/${statusId}`);
    }

    getApvRegister(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/apv-register/${from}/${to}/${statusId}`);
    }

    getAjRegister(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/aj-register/${from}/${to}/${statusId}`);
    }

    getMirRegister(from: string, to: string, statusId: number, docType: string = ''): Observable<any[]> {
        const base = docType ? `${BASE_API}/reports/data/mir/${docType}/${from}/${to}/${statusId}`
                             : `${BASE_API}/reports/data/mir/${from}/${to}/${statusId}`;
        return this.http.get<any[]>(base);
    }

    getSalesRegister(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/sales-register/${from}/${to}/${statusId}`);
    }

    getCashRegister(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/cash-register/${from}/${to}/${statusId}`);
    }

    getJvRegisterRecap(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/jv-register-recap/${from}/${to}/${statusId}`);
    }

    getCvRegisterRecap(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/cv-register-recap/${from}/${to}/${statusId}`);
    }

    getApvRegisterRecap(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/apv-register-recap/${from}/${to}/${statusId}`);
    }

    getAjRegisterRecap(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/aj-register-recap/${from}/${to}/${statusId}`);
    }

    getSalesRegisterRecap(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/sales-register-recap/${from}/${to}/${statusId}`);
    }

    getCashRegisterRecap(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/cash-register-recap/${from}/${to}/${statusId}`);
    }

    getMirRegisterRecap(from: string, to: string, statusId: number, docType: string = ''): Observable<any[]> {
        const base = docType ? `${BASE_API}/reports/data/mir-recap/${docType}/${from}/${to}/${statusId}`
                             : `${BASE_API}/reports/data/mir-recap/${from}/${to}/${statusId}`;
        return this.http.get<any[]>(base);
    }

    getPoSummary(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/purchase-order/list/${from}/${to}/${statusId}`);
    }

    getJoSummary(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/job-order/list/${from}/${to}/${statusId}`);
    }

    getJoaSummary(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/jo-acceptance/list/${from}/${to}/${statusId}`);
    }

    getCanvassSummary(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/canvass/list/${from}/${to}/${statusId}`);
    }

    getPrSummary(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/payment-request/list/${from}/${to}/${statusId}`);
    }

    getRvSummary(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/api/requisition-voucher/list/${from}/${to}/${statusId}`);
    }

    getPcfLedger(from: string, to: string, docStatId: number, pcfId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/petty-cash-fund-ledger/${from}/${to}/${docStatId}/${pcfId}`);
    }

    getForm1601ESchedule(year: number, month: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/form-1601E-schedule/${year}/${month}`);
    }

    getPeSummary(month: number, year: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/pe-summary/${month}/${year}`);
    }

    getQuotationSummaryItems(rivId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/quotation-summary/${rivId}`);
    }

    getPendingVoucherList(docTypeId: number, tableName: string, pt: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/data/pending-voucher-list/tn/${tableName}/pt/${pt}`, {
            params: new HttpParams().set('docTypeId', docTypeId).set('statusId', statusId)
        });
    }
}
