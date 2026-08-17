import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class PaymentRequestService {
    private http = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/payment-request/list/${from}/${to}`);
    }

    listByStatus(from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/payment-request/list/${from}/${to}/${statusId}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/payment-request/document-statuses`);
    }

    getData(id: number): Observable<any> {
        return this.http.get<any>(`${BASE_API}/payment-request/${id}`);
    }

    create(payload: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/payment-request/create`, payload);
    }

    update(payload: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/payment-request/update`, payload);
    }

    process(payload: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/payment-request/process`, payload);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/payment-request/export/${id}`, { type: 'pdf' });
    }

    searchVendors(search: string, page: number, size: number): Observable<any> {
        const params = new HttpParams()
            .set('search', search)
            .set('page', page)
            .set('size', size);
        return this.http.get<any>(`${BASE_API}/json/entities`, { params });
    }

    getBudgetLineItems(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/budget-line-items`);
    }

    getBudgetSubItems(budgetLineItemDetailId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/budget-sub-items/${budgetLineItemDetailId}`);
    }

    getFiles(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/payment-request/${id}/files`);
    }

    uploadFiles(id: number, formData: FormData): Observable<any> {
        return this.http.post(`${BASE_API}/payment-request/${id}/upload`, formData);
    }

    deleteFile(fileId: number): Observable<any> {
        return this.http.delete(`${BASE_API}/payment-request/file/${fileId}`);
    }

    fileUrl(fileId: number): string {
        return `${BASE_API}/payment-request/file/${fileId}`;
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }
}
