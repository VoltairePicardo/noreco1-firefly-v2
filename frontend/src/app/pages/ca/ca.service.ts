import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class CaService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(from?: string, to?: string, statusId?: number, officeId?: number): Observable<any[]> {
        let params = new HttpParams();
        if (from)     params = params.set('from',     from);
        if (to)       params = params.set('to',       to);
        if (statusId) params = params.set('statusId', statusId.toString());
        if (officeId) params = params.set('officeId', officeId.toString());
        return this.http.get<any[]>(`${BASE_API}/cash-advance/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/cash-advance/${id}`);
    }

    create(form: any): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        return this.http.post(`${BASE_API}/cash-advance/create`, fd);
    }

    update(form: any): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        return this.http.post(`${BASE_API}/cash-advance/update`, fd);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/cash-advance/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cash-advance/document-statuses`);
    }

    getOffices(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/offices`);
    }

    getUserOffice(): Observable<any> {
        return this.http.get(`${BASE_API}/json/office-user`);
    }

    getBudgetLineItems(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/budget-line-items`);
    }

    getUnliquidatedList(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cash-advance/unliquidated/list`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/cash-advance/default-signatories`);
    }

    getUnits(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/unit-measure/all`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    setAsLiquidated(id: number): Observable<any> {
        return this.http.post(`${BASE_API}/cash-advance/liquidated/${id}`, {}, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/cash-advance/export/${id}`, { type: 'pdf' });
    }
}
