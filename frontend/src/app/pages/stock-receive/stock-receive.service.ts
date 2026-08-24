import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class StockReceiveService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/stock-receive/list`);
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/stock-receive/list/${from}/${to}/${statusId}`
            : `${BASE_API}/stock-receive/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/stock-receive/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/stock-receive/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/stock-receive/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/stock-receive/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/stock-receive/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/stock-receive/export/${id}`, { type: 'pdf' });
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/stock-receive/default-signatories`);
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/stock-receive/inventory-locations`);
    }

    getReceivingDocuments(locationId: number, q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/stock-receive/receiving-documents/${locationId}`, { params });
    }
}
