import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl'); // http://localhost:8080 (for non-/api controllers)
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

    create(form: any, files: File[] = []): Observable<any> {
        const formData = new FormData();
        formData.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        files.forEach(f => formData.append('file_', f, f.name));
        return this.http.post(`${BASE_API}/general-journal/create`, formData);
    }

    update(form: any, files: File[] = [], filesToRemove: any[] = []): Observable<any> {
        const formData = new FormData();
        formData.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        files.forEach(f => formData.append('file_', f, f.name));
        if (filesToRemove.length > 0) {
            formData.append('filesToRemove', new Blob([JSON.stringify(filesToRemove)], { type: 'application/json' }));
        }
        return this.http.post(`${BASE_API}/general-journal/update`, formData);
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
        const payloads = ids.map(id => ({ documentId: id, remarks: '', documentType: 'JV' }));
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

    getTempBatches(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/temp/all`);
    }

    getTempGLEntries(tempBatchId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/temp/gl/${tempBatchId}`);
    }

    getAccountSettingEntries(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/account-setting/${transId}`);
    }

    getCcprBatchesForJv(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/credit-card-purchase-request/batches-for-jv`);
    }

    getCcprRequestsByBatch(batchId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/credit-card-purchase-request/batch/${batchId}`);
    }

    getCalForJv(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/cash-advance-liquidation/for-jv`, { params });
    }

    getMctForJv(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/stock-receive/for-jv-approved-paged`, { params });
    }

    getStockAdjustmentForJv(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/stock-adjustment/for-jv-approved-paged`, { params });
    }

    getRrForJv(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/rr/for-jv`, { params });
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/general-journal/export/${id}`, { type: 'pdf' });
    }
}
