import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class CaLiquidationService {
    private http = inject(HttpClient);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/cash-advance-liquidation/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_URL}/cash-advance-liquidation/${id}`);
    }

    create(form: any): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        return this.http.post(`${BASE_URL}/cash-advance-liquidation/create`, fd);
    }

    update(form: any): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        return this.http.post(`${BASE_URL}/cash-advance-liquidation/update`, fd);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_URL}/cash-advance-liquidation/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    getCashAdvanceList(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/cash-advance/list`);
    }

    getParticularsForLiquidation(caId: number, calId = 0): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/cap/details-for-liquidation/${caId}/${calId}`);
    }
}
