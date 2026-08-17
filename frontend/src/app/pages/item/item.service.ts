import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API  = environment.get('baseApiUrl');

const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class ItemService {
    private http = inject(HttpClient);

    list(q = '', accountId: number | null = null, page = 0, size = 10, categoryId: number | null = null): Observable<any> {
        let params = new HttpParams().set('q', q).set('page', page).set('size', size);
        if (accountId != null) params = params.set('accountId', accountId);
        if (categoryId != null) params = params.set('categoryId', categoryId);
        return this.http.get(`${BASE_API}/item/list`, { params });
    }

    getCategories(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/inventory-categories`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/item/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/item/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/item/update`, form, httpOptions);
    }

    listUnits(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/unit-measure/all`);
    }

    listCategories(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/inventory-category/list`);
    }

    uploadImage(file: File, itemId: number): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('itemId', String(itemId));
        return this.http.post(`${BASE_API}/item/upload-image`, formData);
    }

    remove(id: number): Observable<any> {
        return this.http.post(`${BASE_API}/item/delete/${id}`, {}, httpOptions);
    }

    // Returns Page<ItemStock> — item.id is itemStockId, item.item contains the actual Item entity
    listByLocation(locationId: number, q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/item-stock/item-stock/list-paged-inv-loc/${locationId}`, { params });
    }
}
