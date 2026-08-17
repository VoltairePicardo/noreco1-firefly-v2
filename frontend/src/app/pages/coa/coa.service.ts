import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class CoaService {
    private http = inject(HttpClient);

    list(): Observable<any> {
        return this.http.get(`${BASE_API}/accounting/accounts/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/accounting/accounts/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/accounting/accounts/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/accounting/accounts/update`, form, httpOptions);
    }

    remove(id: number): Observable<any> {
        return this.http.post(`${BASE_API}/accounting/accounts/delete/${id}`, {}, httpOptions);
    }

    getAccountTypes(): Observable<any> {
        return this.http.get(`${BASE_API}/json/account-types`);
    }

    getAccountGroups(): Observable<any> {
        return this.http.get(`${BASE_API}/json/account-groups`);
    }

    getFactors(): Observable<any> {
        return this.http.get(`${BASE_API}/factor/all`);
    }
}
