import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';
import { PurchaseOrderForItemTestingPage } from '@/app/models/inventory-modules/purchase-order.model';

const BASE_API = environment.get('baseApiUrl');

const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

const DOCUMENT_STATUS_APPROVED = 7;

@Injectable({ providedIn: 'root' })
export class PurchaseOrderService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    listByDateRange(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/purchase-order/list/${from}/${to}/${statusId}`
            : `${BASE_API}/purchase-order/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/purchase-order/document-statuses`);
    }

    getDeliveryTerms(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/purchase-order/delivery-terms`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/purchase-order/${id}`);
    }

    getDetails(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/po-detail/pod/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/purchase-order/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/purchase-order/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/purchase-order/process`, payload, httpOptions);
    }

    supplierReceived(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/purchase-order/supplier-received`, payload, httpOptions);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.post<any[]>(`${BASE_API}/document/${transId}/logs`, {}, httpOptions);
    }

    getCanvassPrice(supplierAccountNo: number, rvDetailId: number): Observable<number> {
        return this.http.get<number>(`${BASE_API}/po-detail/canvass-price/${supplierAccountNo}/${rvDetailId}`);
    }

    getRvDetailsForPo(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/rvd/type/canvass`);
    }

    getRvDetailsForQuotation(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/rvd/type/quotation`);
    }

    getPurchaseRequestsForPo(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/purchase-request/for-po`);
    }

    getPurchaseRequestItems(prId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/rv-detail/rvd/${prId}`);
    }

    getFiles(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/purchase-order/${id}/files`);
    }

    uploadFiles(id: number, formData: FormData): Observable<any> {
        return this.http.post(`${BASE_API}/purchase-order/${id}/upload`, formData);
    }

    deleteFile(fileId: number): Observable<any> {
        return this.http.delete(`${BASE_API}/purchase-order/file/${fileId}`);
    }

    fileUrl(fileId: number): string {
        return `${BASE_API}/purchase-order/file/${fileId}`;
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/purchase-order/export/${id}`, { type: 'pdf' });
    }

    getForItemTestingPaged(query = '', page = 0, size = 10): Observable<PurchaseOrderForItemTestingPage> {
        const params = new HttpParams().set('q', query).set('page', page).set('size', size);
        return this.http.get<PurchaseOrderForItemTestingPage>(
            `${BASE_API}/purchase-order/for-item-testing/${DOCUMENT_STATUS_APPROVED}/paged`,
            { params }
        );
    }
}
