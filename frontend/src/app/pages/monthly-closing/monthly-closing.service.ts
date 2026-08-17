import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class MonthlyClosingService {
    private http = inject(HttpClient);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/monthly-closing/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/monthly-closing/${id}`);
    }

    getLogs(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/monthly-closing/${id}/logs`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/monthly-closing/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/monthly-closing/update`, form, httpOptions);
    }
}
