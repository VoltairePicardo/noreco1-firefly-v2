import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class GeneralJournalService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/general-journal/list`);
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/general-journal/list/${from}/${to}/${statusId}`
            : `${BASE_API}/general-journal/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/general-journal/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/general-journal/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/general-journal/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/general-journal/update`, form, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/general-journal/process`, payload, httpOptions);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/general-journal/default-signatories`);
    }

    getEntities(q: string = '', page = 0, size = 10, entityTypes?: number[], classification?: string | null): Observable<any> {
        let params = new HttpParams().set('q', q).set('page', page).set('size', size);
        if (entityTypes?.length) {
            entityTypes.forEach(t => params = params.append('entityTypes', t));
        }
        if (classification) {
            params = params.set('classification', classification);
        }
        return this.http.get(`${BASE_API}/json/entities/search`, { params });
    }

    getEntityClassifications(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/sl-entity-classifications`);
    }

    searchAccounts(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/accounting/accounts/search`, { params });
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    approveAll(ids: number[]): Observable<any> {
        return this.http.post<any>(`${BASE_API}/general-journal/approve-all`, { ids }, httpOptions);
    }

    getFiles(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/general-journal/${id}/files`);
    }

    uploadFiles(id: number, formData: FormData): Observable<any> {
        return this.http.post(`${BASE_API}/general-journal/${id}/upload`, formData);
    }

    deleteFile(fileId: number): Observable<any> {
        return this.http.delete(`${BASE_API}/general-journal/file/${fileId}`);
    }

    fileUrl(fileId: number): string {
        return `${BASE_API}/general-journal/file/${fileId}`;
    }

    getTempBatches(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/temp/all`);
    }

    getTempGLEntries(tempBatchId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/temp/gl/${tempBatchId}`);
    }

    getAccountSettingEntries(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/account-setting/${transId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/general-journal/export/${id}`, { type: 'pdf' });
    }
}
