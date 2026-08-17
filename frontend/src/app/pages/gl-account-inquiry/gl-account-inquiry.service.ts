import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class GlAccountInquiryService {
    private http = inject(HttpClient);

    getDetail(accountId: number, from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/gl-account-inquiry/${accountId}/${from}/${to}/${statusId}`);
    }

    getSummary(accountId: number, from: string, to: string, statusId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/gl-account-inquiry/summary/${accountId}/${from}/${to}/${statusId}`);
    }

    getDocumentStatuses(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/document-statuses`);
    }

    getDefaultSignatories(): Observable<any> {
        return this.http.get<any>(`${BASE_API}/reports/accounting/gl-account-inquiry/default-signatories`);
    }
}
