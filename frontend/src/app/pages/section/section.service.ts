import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class SectionService {
    private http = inject(HttpClient);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/section/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/section/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/section/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/section/update`, form, httpOptions);
    }
}
