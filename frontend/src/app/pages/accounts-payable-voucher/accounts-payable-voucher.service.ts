import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class AccountsPayableVoucherService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    getList(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/apv/list/${from}/${to}/${statusId}`
            : `${BASE_API}/apv/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getById(id: number): Observable<any> {
        return this.http.get<any>(`${BASE_API}/apv/${id}`);
    }

    create(payload: any, files: File[] = []): Observable<any> {
        const formData = new FormData();
        formData.append('model', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
        files.forEach(f => formData.append('files', f, f.name));
        return this.http.post<any>(`${BASE_API}/apv/create`, formData);
    }

    update(payload: any, files: File[] = [], filesToRemove: any[] = []): Observable<any> {
        const formData = new FormData();
        formData.append('model', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
        files.forEach(f => formData.append('files', f, f.name));
        if (filesToRemove.length > 0) {
            formData.append('filesToRemove', new Blob([JSON.stringify(filesToRemove)], { type: 'application/json' }));
        }
        return this.http.post<any>(`${BASE_API}/apv/update`, formData);
    }

    process(dto: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/apv/process`, dto);
    }

    approveAll(ids: number[]): Observable<any> {
        const payloads = ids.map(id => ({ documentId: id, remarks: '', documentType: 'APV' }));
        return this.http.post<any>(`${BASE_API}/approve-vouchers/process-all`, payloads, httpOptions);
    }

    getJournalEntries(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/gl/${transId}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/apv/document-statuses`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get<any>(`${BASE_API}/apv/default-signatories`);
    }

    searchVendors(search: string, page: number, size: number): Observable<any> {
        const params = new HttpParams()
            .set('search', search)
            .set('page', page)
            .set('size', size);
        return this.http.get<any>(`${BASE_API}/json/entities`, { params });
    }

    searchRvs(q: string = '', page: number = 0, size: number = 10): Observable<any> {
        const params = new HttpParams()
            .set('q', q)
            .set('page', page)
            .set('size', size);
        return this.http.get<any>(`${BASE_API}/apv/rr-approved-paged`, { params });
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/apv/export/${id}`, { type: 'pdf' });
    }
}
