import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');

const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class EnergySalesService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/sales-voucher/list`);
    }

    listByStatus(statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/sales-voucher/list/${statusId}`);
    }

    listByDateRange(from: string, to: string, statusId: number | null = null): Observable<any[]> {
        let params = new HttpParams().set('from', from).set('to', to);
        if (statusId != null && statusId !== 0) params = params.set('statusId', statusId);
        return this.http.get<any[]>(`${BASE_API}/sales-voucher/list/date-range`, { params });
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/sales-voucher/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/sales-voucher/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/sales-voucher/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/sales-voucher/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/sales-voucher/process`, payload, httpOptions);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/sales-voucher/default-signatories`);
    }

    getDefaultDate(): Observable<any> {
        return this.http.get(`${BASE_API}/sales-voucher/default-date`);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    searchAccounts(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/accounting/accounts/search`, { params });
    }

    getJournalEntries(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/gl/${transId}`);
    }

    updateEntries(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/sales-voucher/update-entries`, payload, httpOptions);
    }

    approveAll(ids: number[], remarks = ''): Observable<any> {
        const payloads = ids.map(id => ({ documentId: id, remarks, documentType: 'SV' }));
        return this.http.post<any>(`${BASE_API}/approve-vouchers/process-all`, payloads, httpOptions);
    }

    getFiles(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/file/attachments/${transId}`);
    }

    fileUrl(fileId: number): string {
        return this.downloadService.printUrl(`${BASE_URL}/file/${fileId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/sales-voucher/export/${id}`, { type: 'pdf' });
    }
}
