import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const BASE_URL = environment.get('baseUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

export type ApvDocumentType = 'PR' | 'RR' | 'JOA';

@Injectable({ providedIn: 'root' })
export class AccountsPayableVoucherService {
    private http            = inject(HttpClient);
    private downloadService = inject(DownloadService);

    getList(from: string, to: string, statusId?: number | null): Observable<any[]> {
        const url = statusId
            ? `${BASE_API}/apv/list/${from}/${to}/${statusId}`
            : `${BASE_API}/apv/list/${from}/${to}`;
        return this.http.get<any[]>(url);
    }

    getById(id: number): Observable<any> {
        return this.http.get<any>(`${BASE_API}/apv/${id}`);
    }

    create(payload: any, files: File[] = []): Observable<any> {
        const formData = new FormData();
        formData.append('model', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
        files.forEach(f => formData.append('file_', f, f.name));
        return this.http.post<any>(`${BASE_API}/apv/create`, formData);
    }

    update(payload: any, files: File[] = [], filesToRemove: any[] = []): Observable<any> {
        const formData = new FormData();
        formData.append('model', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
        files.forEach(f => formData.append('file_', f, f.name));
        if (filesToRemove.length > 0) {
            formData.append('filesToRemove', new Blob([JSON.stringify(filesToRemove)], { type: 'application/json' }));
        }
        return this.http.post<any>(`${BASE_API}/apv/update`, formData);
    }

    process(dto: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/apv/process`, dto);
    }

    approveAll(ids: number[]): Observable<any> {
        const payloads = ids.map(id => ({ documentId: id, remarks: '', documentType: 'APV' }));
        return this.http.post<any>(`${BASE_API}/approve-vouchers/process-all`, payloads, httpOptions);
    }

    getJournalEntries(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/ledger/gl/${transId}`);
    }

    getFiles(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_URL}/file/attachments/${transId}`);
    }

    fileUrl(fileId: number): string {
        return this.downloadService.printUrl(`${BASE_URL}/file/${fileId}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/apv/document-statuses`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get<any>(`${BASE_API}/apv/default-signatories`);
    }

    searchVendors(search: string, page: number, size: number): Observable<any> {
        const params = new HttpParams()
            .set('search', search)
            .set('page', page)
            .set('size', size);
        return this.http.get<any>(`${BASE_API}/json/entities`, { params });
    }

    searchPurchasingDocuments(type: ApvDocumentType, q: string = '', page: number = 0, size: number = 10, supplierAccountNo?: number | null): Observable<any> {
        let params = new HttpParams()
            .set('q', q)
            .set('page', page)
            .set('size', size);
        if (type === 'RR') {
            params = params.set('s', supplierAccountNo as number);
        }
        const path = type === 'PR' ? 'pr-approved-paged' : type === 'JOA' ? 'joa-approved-paged' : 'rr-approved-with-account-setting-paged';
        return this.http.get<any>(`${BASE_API}/apv/${path}`, { params });
    }

    getLinkedPaymentRequest(apvId: number): Observable<any> {
        return this.http.get<any>(`${BASE_API}/apv/${apvId}/payment-request`);
    }

    getLinkedJoAcceptance(apvId: number): Observable<any> {
        return this.http.get<any>(`${BASE_API}/apv/${apvId}/jo-acceptance`);
    }

    getReceivingReport(id: number): Observable<any> {
        return this.http.get<any>(`${BASE_API}/rr/${id}`);
    }

    getSupplierAccount(): Observable<any> {
        return this.http.get<any>(`${BASE_API}/accounting/accounts/supplier-account`);
    }

    getWorkflowActions(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/workflow-actions/${transId}`);
    }

    getDocumentLogs(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-logs/${transId}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/apv/export/${id}`, { type: 'pdf' });
    }
}
