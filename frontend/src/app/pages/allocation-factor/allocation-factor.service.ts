import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class AllocationFactorService {
    private http = inject(HttpClient);

    list(q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/factor/list`, { params });
    }

    getById(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/factor/${id}`);
    }

    getByIdAndValidity(factorId: number, validityId: number): Observable<any> {
        return this.http.get(`${BASE_API}/factor/${factorId}/validity/${validityId}`);
    }

    create(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/factor/create`, payload);
    }

    updateByValidity(payload: any): Observable<any> {
        return this.http.post(`${BASE_API}/factor/update-by-validity`, payload);
    }

    delete(id: number): Observable<any> {
        return this.http.post(`${BASE_API}/factor/delete/${id}`, {});
    }

    getDateRanges(): Observable<any> {
        return this.http.get(`${BASE_API}/allocation-factor/date-ranges`);
    }

    getBusinessSegments(): Observable<any> {
        return this.http.get(`${BASE_API}/json/business-segments`);
    }
}
