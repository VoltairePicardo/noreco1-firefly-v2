import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class StockAdjustmentService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/stock-adjustment/list`);
    }

    listPaged(from: string, to: string, statusId: number | null, query: string, page: number, size: number): Observable<any> {
        let params = new HttpParams().set('from', from).set('to', to).set('page', page).set('size', size);
        if (statusId != null) params = params.set('statusId', statusId);
        if (query) params = params.set('query', query);
        return this.http.get(`${BASE_API}/stock-adjustment/list-paged`, { params });
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/stock-adjustment/list/${from}/${to}/${statusId}`
            : `${BASE_API}/stock-adjustment/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/stock-adjustment/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/stock-adjustment/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/stock-adjustment/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/stock-adjustment/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/stock-adjustment/process`, payload, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/stock-adjustment/export/${id}`, { type: 'pdf' });
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/stock-adjustment/default-signatories`);
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/stock-adjustment/inventory-locations`);
    }

    getItemStocks(locationId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/stock-adjustment/item-stocks/${locationId}`);
    }
}
