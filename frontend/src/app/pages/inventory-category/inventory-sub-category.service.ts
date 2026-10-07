import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { InventorySubCategory } from '@/app/models/coop-accounting/inventory-category.model';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class InventorySubCategoryService {
    private http = inject(HttpClient);

    getData(id: number): Observable<InventorySubCategory> {
        return this.http.get<InventorySubCategory>(`${BASE_API}/inventory-sub-category/${id}`);
    }

    create(form: InventorySubCategory): Observable<any> {
        return this.http.post(`${BASE_API}/inventory-sub-category/create`, form, httpOptions);
    }

    update(form: InventorySubCategory): Observable<any> {
        return this.http.post(`${BASE_API}/inventory-sub-category/update`, form, httpOptions);
    }
}
