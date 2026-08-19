import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class CashReceiptsService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cash-receipts/list`);
    }

    listByStatus(statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cash-receipts/list/${statusId}`);
    }

    listByDateRange(from: string, to: string, statusId: number | null = null): Observable<any[]> {
        let params = new HttpParams().set('from', from).set('to', to);
        if (statusId != null && statusId !== 0) params = params.set('statusId', statusId);
        return this.http.get<any[]>(`${BASE_API}/cash-receipts/list/date-range`, { params });
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cash-receipts/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/cash-receipts/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/cash-receipts/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/cash-receipts/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/cash-receipts/process`, payload, httpOptions);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/cash-receipts/default-signatories`);
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

    approveAll(ids: number[]): Observable<any> {
        const payloads = ids.map(id => ({ documentId: id, remarks: '', documentType: 'CRV' }));
        return this.http.post<any>(`${BASE_API}/approve-vouchers/process-all`, payloads, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/cash-receipts/export/${id}`, { type: 'pdf' });
    }
}
