import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class BankAccountService {
    private http = inject(HttpClient);

    list(q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/bank-account/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/bank-account/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/bank-account/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/bank-account/update`, form, httpOptions);
    }

    listBanks(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank/all`);
    }

    listTransactionTypes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/bank-transaction-type/list`);
    }

    listAccounts(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/accounting/accounts/list`);
    }
}
