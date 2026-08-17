import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class RrService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rr/list`);
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/rr/list/${from}/${to}/${statusId}`
            : `${BASE_API}/rr/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rr/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/rr/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/rr/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/rr/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/rr/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.post<any[]>(`${BASE_URL}/document/${transId}/logs`, {}, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_URL}/rr/export/${id}`, { type: 'pdf' });
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rr/inventory-locations`);
    }

    getPurchaseOrderDetailsForRR(poId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rr/po-details/${poId}`);
    }

    getPurchaseOrderDetailsWithItemTesting(poId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rr/po-details-item-testing/${poId}`);
    }

    getJobOrderDetailsForRR(joId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rr/jo-details/${joId}`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/rr/default-signatories`);
    }
}
