import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
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

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/disbursement/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/disbursement/update`, form, httpOptions);
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

    getBankAccounts(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank-account/all`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    approveAll(ids: number[]): Observable<any> {
        const payloads = ids.map(id => ({ documentId: id, remarks: '', documentType: 'CV' }));
        return this.http.post<any>(`${BASE_API}/approve-vouchers/process-all`, payloads, httpOptions);
    }

    getFiles(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/disbursement/${id}/files`);
    }

    uploadFiles(id: number, formData: FormData): Observable<any> {
        return this.http.post(`${BASE_API}/disbursement/${id}/upload`, formData);
    }

    deleteFile(fileId: number): Observable<any> {
        return this.http.delete(`${BASE_API}/disbursement/file/${fileId}`);
    }

    fileUrl(fileId: number): string {
        return `${BASE_API}/disbursement/file/${fileId}`;
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/disbursement/export/${id}`, { type: 'pdf' });
    }
}
