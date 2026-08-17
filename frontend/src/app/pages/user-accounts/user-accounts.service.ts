import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class UserAccountsService {
    private http = inject(HttpClient);

    list(searchText = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams()
            .set('searchText', searchText)
            .set('page', page)
            .set('size', size);
        return this.http.get(`${BASE_API}/user/pageable`, { params });
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/user/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/user/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/user/update`, form, httpOptions);
    }

    uploadSignature(file: File, userId: number): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('userId', String(userId));
        return this.http.post(`${BASE_API}/user/upload-signature`, formData);
    }
}
