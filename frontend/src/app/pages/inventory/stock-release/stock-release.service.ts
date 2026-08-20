import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';
import { HalPage, toPagedResult } from '@/app/models/hal-page.model';
import { InventoryDocumentDto, ReleasingDocumentType } from '@/app/models/inventory-document.model';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class StockReleaseService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    listPaged(from: string, to: string, statusId: number | null, query: string, page: number, size: number): Observable<any> {
        let params = new HttpParams().set('from', from).set('to', to).set('page', page).set('size', size);
        if (statusId != null) params = params.set('statusId', statusId);
        if (query) params = params.set('query', query);
        return this.http.get(`${BASE_API}/stock-release/list-paged`, { params });
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/stock-release/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/stock-release/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/stock-release/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/stock-release/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/stock-release/process`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/json/workflow-actions/${transId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/stock-release/export/${id}`, { type: 'pdf' });
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get(`${BASE_API}/stock-release/default-signatories`);
    }

    getReleasingDocuments(
        type: ReleasingDocumentType, query: string, page: number, size: number
    ): Observable<{ content: InventoryDocumentDto[]; totalElements: number }> {
        let params = new HttpParams().set('t', type).set('page', page).set('size', size);
        if (query) params = params.set('q', query);
        return this.http.get<HalPage<InventoryDocumentDto>>(`${BASE_API}/stock-release/documents`, { params })
            .pipe(map(toPagedResult));
    }
}
