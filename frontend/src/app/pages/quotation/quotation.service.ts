import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class QuotationService {
    private http = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/quotation/list/${from}/${to}`);
    }

    listByStatus(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/quotation/list/${from}/${to}/${statusId}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/quotation/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get<any>(`${BASE_API}/quotation/${id}`);
    }

    getDetails(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/quotation/${id}/details`);
    }

    getBrands(): Observable<any[]> {
        const params = new HttpParams().set('q', '').set('page', 0).set('size', 500);
        return this.http.get<any>(`${BASE_API}/brand/list`, { params });
    }

    create(payload: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/quotation/create`, payload);
    }

    update(payload: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/quotation/update`, payload);
    }

    process(payload: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/quotation/process`, payload);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/quotation/export/${id}`, { type: 'pdf' });
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    searchRv(search: string, page: number, size: number): Observable<any> {
        const params = new HttpParams()
            .set('q', search)
            .set('page', page)
            .set('size', size);
        return this.http.get<any>(`${BASE_API}/purchase-request/list`, { params });
    }

    searchSuppliers(search: string, page: number, size: number): Observable<any> {
        const params = new HttpParams()
            .set('q', search)
            .set('page', page)
            .set('size', size);
        return this.http.get<any>(`${BASE_API}/supplier/list`, { params });
    }

    searchItems(search: string, page: number, size: number): Observable<any> {
        const params = new HttpParams()
            .set('q', search)
            .set('page', page)
            .set('size', size);
        return this.http.get<any>(`${BASE_API}/item/list`, { params });
    }
}
