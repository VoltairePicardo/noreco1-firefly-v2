import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class BudgetSubItemService {
    private http = inject(HttpClient);

    getTreeData(year?: number | null, divisionId?: number | null, search = ''): Observable<any[]> {
        let params = new HttpParams();
        if (year) params = params.set('year', year);
        if (divisionId) params = params.set('divisionId', divisionId);
        if (search) params = params.set('search', search);
        return this.http.get<any[]>(`${BASE_API}/budget-sub-item/tree`, { params });
    }

    getSubItems(budgetLineItemDetailId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/budget-sub-item/list/${budgetLineItemDetailId}`);
    }

    create(dto: any): Observable<any> {
        return this.http.post(`${BASE_API}/budget-sub-item/create`, dto, httpOptions);
    }

    delete(id: number): Observable<any> {
        return this.http.post(`${BASE_API}/budget-sub-item/delete/${id}`, {}, httpOptions);
    }

    getDivisions(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/divisions`);
    }
}
