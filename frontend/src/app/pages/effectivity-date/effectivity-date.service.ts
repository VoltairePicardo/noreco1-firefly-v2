import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class EffectivityDateService {
    private http = inject(HttpClient);

    list(): Observable<any> {
        return this.http.get(`${BASE_API}/effect-date/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/effect-date/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/effect-date/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/effect-date/update`, form, httpOptions);
    }

    remove(id: number): Observable<any> {
        return this.http.post(`${BASE_API}/effect-date/delete/${id}`, {}, httpOptions);
    }
}
