import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class BudgetLineItemService {
    private http = inject(HttpClient);

    list(year?: number | null, deptId?: number | null, divId?: number | null, statusId?: number | null): Observable<any[]> {
        let params = new HttpParams();
        if (year)     params = params.set('y',   year.toString());
        if (deptId)   params = params.set('dep', deptId.toString());
        if (divId)    params = params.set('div', divId.toString());
        if (statusId) params = params.set('s',   statusId.toString());
        return this.http.get<any[]>(`${BASE_API}/budget-line-item/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/budget-line-item/${id}`);
    }

    create(form: any): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        return this.http.post(`${BASE_API}/budget-line-item/create`, fd);
    }

    update(form: any): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        return this.http.post(`${BASE_API}/budget-line-item/update`, fd);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/budget-line-item/process`, payload, httpOptions);
    }

    delete(id: number): Observable<any> {
        return this.http.delete(`${BASE_API}/budget-line-item/${id}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/budget-line-item/document-statuses`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/budget-line-item/default-signatories`);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDepartments(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/departments`);
    }

    getDivisions(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/divisions`);
    }

    getUserDepartment(): Observable<any> {
        return this.http.get(`${BASE_API}/json/user-department`);
    }

    getUserDivision(): Observable<any> {
        return this.http.get(`${BASE_API}/json/user-division`);
    }

    getUnits(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/unit-measure/all`);
    }

    getProjectTypes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/project-types-for-budget-line-item`);
    }

    getStrategicInitiatives(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/strategic-initiatives-for-budget-line-item`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    getNeaPriceByItem(itemId: number): Observable<any> {
        return this.http.get(`${BASE_API}/nea-price-index/by-item/${itemId}`);
    }

    approveAll(payload: any[]): Observable<any> {
        return this.http.post(`${BASE_API}/approve-vouchers/process-all-budget`, payload, httpOptions);
    }
}
