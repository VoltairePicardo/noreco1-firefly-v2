import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { OtherSpecialEquipmentTestingData, OtherSpecialEquipmentTestingSpecialEquipment } from '@/app/models/special-equipment-testing/other-special-equipment-testing.model';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class OtherSpecialEquipmentTestingService {
    private http = inject(HttpClient);

    list(query = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('query', query).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/other-special-equipment-testing/list`, { params });
    }

    getData(id: number): Observable<OtherSpecialEquipmentTestingData> {
        return this.http.get<OtherSpecialEquipmentTestingData>(`${BASE_API}/other-special-equipment-testing/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/other-special-equipment-testing/create`, form, httpOptions);
    }

    searchSpecialEquipment(q: string, page = 0, size = 10): Observable<{ content: OtherSpecialEquipmentTestingSpecialEquipment[] }> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get<{ content: OtherSpecialEquipmentTestingSpecialEquipment[] }>(`${BASE_API}/json/special-equipment/search`, { params });
    }
}
