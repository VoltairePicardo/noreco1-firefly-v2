import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class VoucherCashflowService {
    private http = inject(HttpClient);

    getAllVouchers(from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/voucher-cash-flow/all-vouchers/${from}/${to}`);
    }

    getVoucher(documentTypeCode: string, voucherId: number): Observable<any> {
        return this.http.get<any>(`${BASE_API}/voucher-cash-flow/${documentTypeCode}/${voucherId}`);
    }

    getDetails(transId: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/voucher-cash-flow/details/${transId}`);
    }

    getCashflowItems(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/cashflow-item/list`);
    }

    save(documentTypeCode: string, payload: any): Observable<any> {
        return this.http.post<any>(`${BASE_API}/voucher-cash-flow/set/${documentTypeCode}`, payload, httpOptions);
    }

    getMonthlyCycle(year: number, month: number): Observable<any> {
        return this.http.get<any>(`${BASE_API}/monthly-cycle/${year}/${month}`);
    }
}
