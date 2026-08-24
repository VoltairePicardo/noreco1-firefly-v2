import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import {InventoryLocation} from '@/app/models/dropdown.model';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class InventoryLocationService {
    private http = inject(HttpClient);

    list(q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/inventory-location/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/inventory-location/${id}`);
    }

    getAllLocations(): Observable<InventoryLocation[]> {
        return this.http.get<InventoryLocation[]>(`${BASE_API}/inventory-location/all`);
    }

    getAccounts(): Observable<any> {
        return this.http.get(`${environment.get('baseApiUrl')}/accounting/accounts/list`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/inventory-location/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/inventory-location/update`, form, httpOptions);
    }
}
