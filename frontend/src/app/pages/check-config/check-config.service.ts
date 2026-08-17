import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class CheckConfigService {
    private http = inject(HttpClient);

    list(): Observable<any> {
        return this.http.get(`${BASE_API}/check-config/list`);
    }

    listAllBankAccounts(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank-account/all`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/check-config/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/check-config/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/check-config/update`, form, httpOptions);
    }

    getTestPrint(id: any): Observable<string> {
        return this.http.get(`${BASE_API}/check-config/${id}/test-print`, { responseType: 'text' });
    }
}
