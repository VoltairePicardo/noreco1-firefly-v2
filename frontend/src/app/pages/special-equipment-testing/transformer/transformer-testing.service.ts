import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { TransformerTestingBrand, TransformerTestingData, TransformerTestingLookup, TransformerTestingTransformer } from '@/app/models/special-equipment-testing/transformer-testing.model';
import { ProcessDocumentPayload } from '@/app/models/shared/workflow.model';
import { DownloadService } from '@/app/core/services/download.service';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class TransformerTestingService {
    private http = inject(HttpClient);
    private downloadService = inject(DownloadService);

    list(query = '', page = 0, size = 10): Observable<any> {
        const params = new HttpParams().set('query', query).set('page', page).set('size', size);
        return this.http.get(`${BASE_API}/transformer-testing/list`, { params });
    }

    getData(id: number): Observable<TransformerTestingData> {
        return this.http.get<TransformerTestingData>(`${BASE_API}/transformer-testing/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/transformer-testing/create`, form, httpOptions);
    }

    process(payload: ProcessDocumentPayload): Observable<any> {
        return this.http.post(`${BASE_API}/transformer-testing/process`, payload, httpOptions);
    }

    print(id: number): void {
        this.downloadService.print(`${BASE_API}/transformer-testing/print/export/${id}`, { type: 'pdf' });
    }

    listBrands():Observable<TransformerTestingBrand[]> {
        return this.http.get<TransformerTestingBrand[]>(`${BASE_API}/json/brands`);
    }

    searchTransformers(q: string, page = 0, size = 10): Observable<{ content: TransformerTestingTransformer[] }> {
        const params = new HttpParams().set('q', q).set('page', page).set('size', size);
        return this.http.get<{ content: TransformerTestingTransformer[] }>(`${BASE_API}/json/transformers/search`, { params });
    }

    listPrimaryVoltages(): Observable<TransformerTestingLookup[]> {
        return this.http.get<TransformerTestingLookup[]>(`${BASE_API}/json/primary-voltages`);
    }

    listSecondaryVoltages(): Observable<TransformerTestingLookup[]> {
        return this.http.get<TransformerTestingLookup[]>(`${BASE_API}/json/secondary-voltages`);
    }

    listTransformerConditions(): Observable<TransformerTestingLookup[]> {
        return this.http.get<TransformerTestingLookup[]>(`${BASE_API}/json/transformer-conditions`);
    }
}
