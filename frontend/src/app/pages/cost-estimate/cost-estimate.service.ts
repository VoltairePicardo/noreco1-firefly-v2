import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class CostEstimateService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(q: string = '', from: string = '', to: string = '', statusId: any = null, page = 0, size = 10): Observable<any> {
        let params = new HttpParams().set('page', page).set('size', size);
        if (q)        params = params.set('q', q);
        if (from)     params = params.set('from', from);
        if (to)       params = params.set('to', to);
        if (statusId) params = params.set('statusId', statusId);
        return this.http.get<any>(`${BASE_API}/cost-estimate/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/cost-estimate/${id}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cost-estimate/document-statuses`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/cost-estimate/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/cost-estimate/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/cost-estimate/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.post<any[]>(`${BASE_URL}/document/${transId}/logs`, {}, httpOptions);
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/inventory-locations`);
    }

    getSetting(code: string): Observable<any> {
        return this.http.get<any>(`${BASE_URL}/json/setting/${code}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_URL}/cost-estimate/export/${id}`, { type: 'pdf' });
    }
}
