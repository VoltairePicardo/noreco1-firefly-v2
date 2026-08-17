import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class PclService {
    private http = inject(HttpClient);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/petty-cash-liquidation/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_URL}/petty-cash-liquidation/${id}`);
    }

    create(form: any): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        return this.http.post(`${BASE_URL}/petty-cash-liquidation/create`, fd);
    }

    update(form: any): Observable<any> {
        const fd = new FormData();
        fd.append('model', new Blob([JSON.stringify(form)], { type: 'application/json' }));
        return this.http.post(`${BASE_URL}/petty-cash-liquidation/update`, fd);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_URL}/petty-cash-liquidation/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    defaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_URL}/petty-cash-liquidation/default-signatories`);
    }

    /** Returns all PCVs — the component filters for Released status client-side */
    getReleasedPCVs(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/pcv/list`);
    }

    /** Returns PCV detail lines for populating liquidation items */
    getPCVDetails(pcvId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/pctd/details/${pcvId}`);
    }
}
