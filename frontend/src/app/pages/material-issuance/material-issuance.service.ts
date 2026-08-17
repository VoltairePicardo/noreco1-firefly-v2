import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class MaterialIssuanceService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/material-issuance/list`);
    }

    listByDateRange(from: string, to: string, statusId?: number | null, docType?: string | null): Observable<any[]> {
        let url = statusId
            ? `${BASE_API}/material-issuance/list/${from}/${to}/${statusId}`
            : `${BASE_API}/material-issuance/list/${from}/${to}`;
        if (docType) url += `/type/${docType}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/material-issuance/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/material-issuance/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/material-issuance/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/material-issuance/update`, form, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/material-issuance/process`, payload, httpOptions);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/material-issuance/default-signatories`);
    }

    getEntities(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_URL}/json/entities/search`, { params });
    }

    searchAccounts(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/accounting/accounts/search`, { params });
    }

    searchInventoryDocs(q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/material-issuance/inventory-docs-paged`, { params });
    }

    getInventoryDocItems(docId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/material-issuance/inventory-doc-items/${docId}`);
    }

    approveAll(ids: number[]): Observable<any> {
        return this.http.post<any>(`${BASE_API}/material-issuance/approve-all`, { ids }, httpOptions);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/document-logs/${transId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/material-issuance/export/${id}`, { type: 'pdf' });
    }
}
