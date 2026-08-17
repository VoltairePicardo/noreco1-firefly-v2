import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };

@Injectable({ providedIn: 'root' })
export class PrepaymentService {
    private http = inject(HttpClient);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/pre-payment/list`);
    }

    listByDateRange(start: string, end: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/pre-payment/list/${start}/${end}`);
    }

    listByStatusAndDateRange(status: string, start: string, end: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/pre-payment/list/${status}/${start}/${end}`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/pre-payment/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/pre-payment/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/pre-payment/update`, form, httpOptions);
    }

    getProcessMonthly(month: number, year: number): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/pre-payment/process-monthly?month=${month}&year=${year}`);
    }

    calculateCost(prepaymentId: number, voucherTransId: number, prepaymentAccountId: number): Observable<any> {
        return this.http.get(`${BASE_API}/pre-payment/calculate-cost?prepaymentId=${prepaymentId}&voucherTransId=${voucherTransId}&prepaymentAccountId=${prepaymentAccountId}`);
    }

    linkVoucher(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/pre-payment/link-voucher`, form, httpOptions);
    }

    getVouchersForLinking(accountNo: number, query: string, page: number): Observable<any> {
        return this.http.get(`${BASE_API}/vouchers-for-prepayment-linking/${accountNo}?q=${encodeURIComponent(query)}&page=${page}&size=10`);
    }
}
