import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class AdjustmentJournalService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/adjustment-journal/list`);
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/adjustment-journal/list/${from}/${to}/${statusId}`
            : `${BASE_API}/adjustment-journal/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/adjustment-journal/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/adjustment-journal/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/adjustment-journal/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/adjustment-journal/update`, form, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/adjustment-journal/process`, payload, httpOptions);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/adjustment-journal/default-signatories`);
    }

    defaultDate(): Observable<any> {
        return this.http.get(`${BASE_API}/aj/default-date`);
    }

    closingDefaultEntries(date: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/aj/closing-default-entries/${date}`);
    }

    reopeningDefaultEntries(year: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/aj/reopening-default-entries/${year}`);
    }

    updateEntries(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/aj/updateEntries`, payload, httpOptions);
    }

    getEntities(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/json/entities/search`, { params });
    }

    searchAccounts(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/accounting/accounts/search`, { params });
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    getGLEntries(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/gl/${transId}`);
    }

    approveAll(ids: number[], remarks: string = ''): Observable<any> {
        const payloads = ids.map(id => ({ documentId: id, remarks, documentType: 'AJ' }));
        return this.http.post<any>(`${BASE_API}/approve-vouchers/process-all`, payloads, httpOptions);
    }

    uploadFiles(id: number, formData: FormData): Observable<any> {
        return this.http.post(`${BASE_API}/adjustment-journal/${id}/upload`, formData);
    }

    getFiles(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/file/attachments/${transId}`);
    }

    fileUrl(fileId: number): string {
        return this.downloadService.printUrl(`${BASE_URL}/file/${fileId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/adjustment-journal/export/${id}`, { type: 'pdf' });
    }
}
