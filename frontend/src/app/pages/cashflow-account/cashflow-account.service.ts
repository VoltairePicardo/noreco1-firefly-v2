import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class CashflowAccountService {
    private http = inject(HttpClient);

    list(): Observable<any> {
        return this.http.get(`${BASE_API}/cashflow-item/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/cashflow-item/${id}`);
    }

    getTypes(): Observable<any> {
        return this.http.get(`${BASE_API}/cashflow-item/types`);
    }

    getAccounts(): Observable<any> {
        return this.http.get(`${BASE_API}/accounting/accounts/list`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/cashflow-item/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/cashflow-item/update`, form, httpOptions);
    }

    getLogs(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/cashflow-item/${id}/logs`);
    }
}
