import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class MstService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mst/list`);
    }

    listPaged(from: string, to: string, statusId: number | null, query: string, page: number, size: number): Observable<any> {
        let params = new HttpParams().set('from', from).set('to', to).set('page', page).set('size', size);
        if (statusId != null) params = params.set('statusId', statusId);
        if (query) params = params.set('query', query);
        return this.http.get(`${BASE_API}/mst/list-paged`, { params });
    }

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/mst/list/${from}/${to}/${statusId}`
            : `${BASE_API}/mst/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mst/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/mst/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/mst/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/mst/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/mst/process`, payload, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/mst/export/${id}`, { type: 'pdf' });
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/mst/default-signatories`);
    }

    getDepartments(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/department/list`);
    }

    getInventoryLocations(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mst/inventory-locations`);
    }

    getItemStocks(locationId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/mst/item-stocks/${locationId}`);
    }
}
