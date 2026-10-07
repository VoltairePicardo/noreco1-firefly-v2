import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { InventoryCategory, InventoryCategoryType } from '@/app/models/coop-accounting/inventory-category.model';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class InventoryCategoryService {
    private http = inject(HttpClient);

    list(q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/inventory-category/list`, { params });
    }

    getData(id: number): Observable<InventoryCategory> {
        return this.http.get<InventoryCategory>(`${BASE_API}/inventory-category/${id}`);
    }

    getTypes(): Observable<InventoryCategoryType[]> {
        return this.http.get<InventoryCategoryType[]>(`${BASE_API}/inventory-category/types`);
    }

    create(form: InventoryCategory): Observable<any> {
        return this.http.post(`${BASE_API}/inventory-category/create`, form, httpOptions);
    }

    update(form: InventoryCategory): Observable<any> {
        return this.http.post(`${BASE_API}/inventory-category/update`, form, httpOptions);
    }
}
