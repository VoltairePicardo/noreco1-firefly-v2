import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class BudgetService {
    private http = inject(HttpClient);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cash-flow-budget/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/cash-flow-budget/${id}`);
    }

    getDetails(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cash-flow-budget/details/${id}`);
    }

    getYears(): Observable<number[]> {
        return this.http.get<number[]>(`${BASE_API}/cash-flow-budget/years`);
    }

    getCashflowItems(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cashflow-item/list`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/cash-flow-budget/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/cash-flow-budget/update`, form, httpOptions);
    }
}
