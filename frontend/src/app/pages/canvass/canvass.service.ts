import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');

const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class CanvassService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/canvass/list/${from}/${to}/${statusId}`
            : `${BASE_API}/canvass/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/canvass/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/canvass/${id}`);
    }

    getDetails(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/canvass-detail/cnvsd/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/canvass/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/canvass/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/canvass/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    getCanvassedRVs(canvassIds: number[]): Observable<any[]> {
        let params = new HttpParams();
        canvassIds.forEach(id => params = params.append('canvassIds', String(id)));
        return this.http.get<any[]>(`${BASE_API}/purchase-request/list/canvass`, { params });
    }

    searchSuppliers(q: string = '', page = 0, size = 20): Observable<any> {
        return this.http.get(`${BASE_API}/supplier/list?q=${encodeURIComponent(q)}&page=${page}&size=${size}`);
    }

    getRvDetailsForCanvass(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/rvd/type/canvass`);
    }

    getPrDetailsForCanvass(prId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/prd-for-canvass/${prId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/canvass-rv/export/${id}`, { type: 'pdf' });
    }
}
