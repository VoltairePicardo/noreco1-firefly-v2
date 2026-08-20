import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class WorkOrderService {
    private http = inject(HttpClient);

    list(statusId: any = null, year: any = null, month: any = null, page = 0, size = 10, search = ''): Observable<any> {
        let params = new HttpParams().set('page', page).set('size', size);
        if (statusId) params = params.set('statusId', statusId);
        if (year)     params = params.set('year', year);
        if (month)    params = params.set('month', month);
        if (search)   params = params.set('search', search);
        return this.http.get<any>(`${BASE_API}/work-order/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/work-order/${id}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/work-order/document-statuses`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/work-order/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/work-order/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/work-order/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.post<any[]>(`${BASE_API}/document/${transId}/logs`, {}, httpOptions);
    }

    getLogs(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/work-order/${id}/logs`);
    }

    getPostedVouchers(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/work-order/${id}/posted-vouchers`);
    }

    getWorkOrderDetail(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/work-order/${id}/detail`);
    }

    closeOut(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/work-order/close-out`, payload, httpOptions);
    }

    postWorkOrder(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/work-order/post`, payload, httpOptions);
    }

}
