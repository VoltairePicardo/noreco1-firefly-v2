import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API    = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class BankReconciliationService {
    private http = inject(HttpClient);

    getList(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank-reconciliation/list`);
    }

    getRcList(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank-reconciliation/rc`);
    }

    processRc(id: number, cleared: boolean): Observable<any> {
        return this.http.post(`${BASE_API}/bank-reconciliation/process-rc`, { id, cleared }, httpOptions);
    }

    getOd(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/bank-reconciliation/od/${id}`);
    }

    createOd(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/bank-reconciliation/od/create`, form, httpOptions);
    }

    updateOd(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/bank-reconciliation/od/update`, form, httpOptions);
    }

    processOd(id: number, cleared: boolean): Observable<any> {
        return this.http.post(`${BASE_API}/bank-reconciliation/process-od`, { id, cleared }, httpOptions);
    }
}
