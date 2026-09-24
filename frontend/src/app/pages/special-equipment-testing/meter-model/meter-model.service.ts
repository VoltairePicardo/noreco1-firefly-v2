import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class MeterModelService {
    private http = inject(HttpClient);

    list(q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/meter-model/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/meter-model/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/meter-model/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/meter-model/update`, form, httpOptions);
    }

    listBrands(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/brands`);
    }

    listMeterTypes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/meter-types`);
    }

    listPhases(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/phases`);
    }

    listCurrents(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/currents`);
    }

    listAccuracyClasses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/accuracy-classes`);
    }

    listMeterForms(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/meter-forms`);
    }
}
