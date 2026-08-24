import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class MctService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mct/list`);
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/mct/list/${from}/${to}/${statusId}/0`
            : `${BASE_API}/mct/list/${from}/${to}/0`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mct/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/mct/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/mct/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/mct/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/mct/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.post<any[]>(`${BASE_URL}/document/${transId}/logs`, {}, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_URL}/mct/export/${id}`, { type: 'pdf' });
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/mct/default-signatories`);
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mct/inventory-locations`);
    }

    getStockReleasesForMct(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mct/stock-releases`);
    }
}
