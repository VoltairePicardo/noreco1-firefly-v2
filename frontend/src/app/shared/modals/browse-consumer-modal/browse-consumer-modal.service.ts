import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { ConsumerMeterPage } from './browse-consumer-modal.model';

const BASE_API = environment.get('baseApiUrl');

@Injectable({ providedIn: 'root' })
export class BrowseConsumerModalService {
    private http = inject(HttpClient);

    list(query = '', page = 0, size = 10): Observable<ConsumerMeterPage> {
        const params = new HttpParams().set('query', query).set('page', page).set('size', size);
        return this.http.get<ConsumerMeterPage>(`${BASE_API}/consumer/list`, { params });
    }
}
