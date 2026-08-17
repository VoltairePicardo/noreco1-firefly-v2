import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class SubSupplierService {
    private http = inject(HttpClient);

    list(q = '', suppId: number | null = null, page = 0, size = 10): Observable<any> {
        let params = new HttpParams().set('q', q).set('page', page).set('size', size);
        if (suppId != null) { params = params.set('suppId', suppId); }
        return this.http.get(`${BASE_API}/sub-supplier/list`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/sub-supplier/${id}`);
    }

    upload(file: File, supplierId: number): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('supplierId', String(supplierId));
        return this.http.post(`${BASE_API}/sub-supplier/upload`, formData);
    }
}
