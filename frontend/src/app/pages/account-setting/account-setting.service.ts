import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

const DOC_BROWSE_ENDPOINTS: Record<string, string> = {
    rr:           'receiving-report/apv-approved-paged',
    stockReceive: 'inventory/receiving/approved-paged',
    mct:          'inventory/mct/approved-paged',
    stockRelease: 'inventory/releasing/approved-paged',
    mst:          'inventory/mst/approved-paged',
    stockAdjust:  'inventory/stock-adjustment/approved-paged',
};

@Injectable({ providedIn: 'root' })
export class AccountSettingService {
    private http = inject(HttpClient);

    listByDateRange(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/account-setting/list/${from}/${to}`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/account-setting/${id}`);
    }

    getDetails(transactionId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/account-setting/detail/${transactionId}`);
    }

    getRrLinkedDetails(rrId: number): Observable<any> {
        return this.http.get(`${BASE_API}/account-setting/rr-linked-details/${rrId}`);
    }

    create(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/account-setting/create`, payload, httpOptions);
    }

    update(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/account-setting/update`, payload, httpOptions);
    }

    browseDocuments(docType: string, q: string = '', page = 0, size = 10): Observable<any> {
        const endpoint = DOC_BROWSE_ENDPOINTS[docType];
        if (!endpoint) return of({ content: [], totalElements: 0 });
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/${endpoint}`, { params });
    }

    getSupplierAccount(): Observable<any> {
        return this.http.get(`${BASE_API}/accounting/accounts/supplier-account`);
    }

}
