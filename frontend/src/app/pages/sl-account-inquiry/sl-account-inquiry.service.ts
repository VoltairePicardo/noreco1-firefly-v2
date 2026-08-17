import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class SlAccountInquiryService {
    private http = inject(HttpClient);

    getData(accountId: number, accountNo: number, from: string, to: string): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/reports/accounting/sl-account-inquiry/${accountId}/${accountNo}/${from}/${to}`);
    }
}
