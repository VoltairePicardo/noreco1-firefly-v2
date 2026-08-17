import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class AssetTypeService {
    private http = inject(HttpClient);

    list(q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/asset-type/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/asset-type/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/asset-type/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/asset-type/update`, form, httpOptions);
    }

    remove(id: number): Observable<any> {
        return this.http.post(`${BASE_API}/asset-type/delete/${id}`, {}, httpOptions);
    }
}
