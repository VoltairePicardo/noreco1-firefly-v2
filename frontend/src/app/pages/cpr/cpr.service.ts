import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class CprService {
    private http = inject(HttpClient);

    listPaged(q: string = '', assetType: string = '', fullyDepreciated: string = '', status: string = '', page = 0, size = 20): Observable<any> {
        let params = new HttpParams()
            .set('q', q)
            .set('page', page)
            .set('size', size);
        if (assetType)        params = params.set('asset-type', assetType);
        if (fullyDepreciated) params = params.set('fully-depreciated', fullyDepreciated);
        if (status)           params = params.set('status', status);
        return this.http.get(`${BASE_API}/cpr/list-paged`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/cpr/${id}`);
    }

    getItems(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cpr/${id}/items`);
    }

    getDetails(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cpr/${id}/details`);
    }

    getDepreciationSchedule(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cpr/${id}/depreciation-schedule`);
    }

    getLinkTypes(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cpr/link-types`);
    }

    getAssetTypes(q: string = '', page = 0, size = 200): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/asset-type/list`, { params });
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/cpr/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/cpr/update`, form, httpOptions);
    }

    process(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/cpr/process`, payload, httpOptions);
    }

    retire(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/cpr/retire`, payload, httpOptions);
    }

    getVouchersForAssetLinking(accountNo: string, q: string = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/vouchers-for-asset-linking/${accountNo}`, { params });
    }

    linkAssetVoucher(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/cpr/link-asset-voucher`, payload, httpOptions);
    }
}
