import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import {MeterTestingData, MeterTestingOption, MeterTestingResult} from '@/app/models/special-equipment-testing/meter-testing.model';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class MeterTestingService {
    private http = inject(HttpClient);
    private downloadService = inject(DownloadService);

    extract(file: File): Observable<MeterTestingResult> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<MeterTestingResult>(`${BASE_API}/meter-testing/upload`, formData);
    }

    listMeterModels(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/meter-model/all`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/meter-testing/create`, form, httpOptions);
    }

    createIndividual(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/meter-testing/create-individual`, form, httpOptions);
    }

    list(q = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/meter-testing/list`, { params });
    }

    getData(id: number): Observable<MeterTestingData> {
        return this.http.get<MeterTestingData>(`${BASE_API}/meter-testing/${id}`);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/meter-testing/print/export/${id}`, { type: 'pdf' });
    }

    listOptions():Observable<MeterTestingOption[]> {
        return this.http.get<MeterTestingOption[]>(`${BASE_API}/meter-testing/options`);
    }
}
