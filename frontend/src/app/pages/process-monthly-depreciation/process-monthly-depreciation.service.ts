import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class ProcessMonthlyDepreciationService {
    private http = inject(HttpClient);

    list(year: number, month: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/process-monthly-depreciation/list/${year}/${month}`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/process-monthly-depreciation/${id}`);
    }

    getDetails(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/process-monthly-depreciation/${id}/detail`);
    }

    getFooter(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/process-monthly-depreciation/${id}/detail/footer`);
    }

    process(payload: { year: number; month: number }): Observable<any> {
        return this.http.post(`${BASE_API}/process-monthly-depreciation/process`, payload, httpOptions);
    }
}
