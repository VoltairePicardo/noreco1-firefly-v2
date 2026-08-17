import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class FundingSourceService {
    private http = inject(HttpClient);

    list(q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/funding-source/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/funding-source/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/funding-source/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/funding-source/update`, form, httpOptions);
    }
}
