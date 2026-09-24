import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import {MeterTestingResult} from '@/app/models/special-equipment-testing/meter-testing.model';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class MeterTestingService {
    private http = inject(HttpClient);

    extract(file: File): Observable<MeterTestingResult> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<MeterTestingResult>(`${BASE_API}/meter-testing/upload`, formData);
    }

    listMeterModels(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/meter-model/all`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/meter-testing/create`, form, httpOptions);
    }

    list(q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/meter-testing/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/meter-testing/${id}`);
    }
}
