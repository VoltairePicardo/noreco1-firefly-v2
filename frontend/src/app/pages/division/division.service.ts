import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class DivisionService {
    private http = inject(HttpClient);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/division/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/division/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/division/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/division/update`, form, httpOptions);
    }
}
