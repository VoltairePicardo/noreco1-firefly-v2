import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl'); // http://localhost:8080 (for non-/api controllers)
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class DisbursementService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/disbursement/list`);
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/disbursement/list/${from}/${to}/${statusId}`
            : `${BASE_API}/disbursement/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/disbursement/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/disbursement/${id}`);
    }

    create(form: any, files: File[] = []): Observable<any> {
        const formData = new FormData();
        formData.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        files.forEach(f => formData.append('file_', f, f.name));
        return this.http.post(`${BASE_API}/disbursement/create`, formData);
    }

    update(form: any, files: File[] = [], filesToRemove: any[] = []): Observable<any> {
        const formData = new FormData();
        formData.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        files.forEach(f => formData.append('file_', f, f.name));
        if (filesToRemove.length > 0) {
            formData.append('filesToRemove', new Blob([JSON.stringify(filesToRemove)], { type: 'application/json' }));
        }
        return this.http.post(`${BASE_API}/disbursement/update`, formData);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/disbursement/process`, payload, httpOptions);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/disbursement/default-signatories`);
    }

    getEntities(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/json/entities/search`, { params });
    }

    searchAccounts(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/accounting/accounts/search`, { params });
    }

    getBankAccountsForCv(bankId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank-account/for-cv/${bankId}`);
    }

    getBanks(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank/all`);
    }

    private approvedForCvPaged(endpoint: string, q: string, page: number, size: number): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/${endpoint}/approved-for-cv-paged`, { params });
    }

    getApprovedApvForCv(q: string = '', page = 0, size = 10): Observable<any> {
        return this.approvedForCvPaged('apv', q, page, size);
    }

    getApprovedCaForCv(q: string = '', page = 0, size = 10): Observable<any> {
        return this.approvedForCvPaged('cash-advance', q, page, size);
    }

    getApprovedJvForCv(q: string = '', page = 0, size = 10): Observable<any> {
        return this.approvedForCvPaged('general-journal', q, page, size);
    }

    getApprovedRrForCv(q: string = '', page = 0, size = 10): Observable<any> {
        return this.approvedForCvPaged('rr', q, page, size);
    }

    getApprovedJoaForCv(q: string = '', page = 0, size = 10): Observable<any> {
        return this.approvedForCvPaged('jo-acceptance', q, page, size);
    }

    getNextCheckNumber(bankAccountId: number): Observable<{ checkNumber?: string }> {
        return this.http.get<{ checkNumber?: string }>(`${BASE_API}/disbursement/next-check-number/${bankAccountId}`);
    }

    getCvChecks(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/disbursement/checks/${transId}`);
    }

    getGLEntries(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/gl/${transId}`);
    }

    getGLEntriesWithoutWithholdingTax(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/gl-without-withholding-tax/${transId}`);
    }

    getGLEntriesForCashAdvance(caId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/gl-for-ca/${caId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    approveAll(ids: number[]): Observable<any> {
        const payloads = ids.map(id => ({ documentId: id, remarks: '', documentType: 'CV' }));
        return this.http.post<any>(`${BASE_API}/approve-vouchers/process-all`, payloads, httpOptions);
    }

    getFiles(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/file/attachments/${transId}`);
    }

    deleteFile(fileId: number): Observable<any> {
        return this.http.delete(`${BASE_URL}/file/${fileId}`);
    }

    fileUrl(fileId: number): string {
        return this.downloadService.printUrl(`${BASE_URL}/file/${fileId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/disbursement/export/${id}`, { type: 'pdf' });
    }

    print2307(payeeAccountNo: number, transId: number): void {
        this.downloadService.print(`${BASE_URL}/check-voucher/print-2307/${payeeAccountNo}/${transId}`);
    }

    print2307SubSuppliers(transId: number): void {
        this.downloadService.print(`${BASE_URL}/check-voucher/print-2307-sub-suppliers/${transId}`);
    }

    printCheck(transId: number, bankAccountId: number): void {
        this.downloadService.print(`${BASE_URL}/check-voucher/print-check-jas/${transId}/${bankAccountId}`, { type: 'pdf' });
    }

    updateCheckNumber(payload: any): Observable<any> {
        return this.http.post(`${BASE_URL}/check-voucher/update-check`, payload, httpOptions);
    }
}
