import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class PositionService {
    private http = inject(HttpClient);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/position/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/position/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/position/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/position/update`, form, httpOptions);
    }

    deleteById(id: number): Observable<any> {
        return this.http.post(`${BASE_API}/position/delete/${id}`, {}, httpOptions);
    }
}
