import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const API_URL = environment.get('baseApiUrl') + '/setting';

@Injectable({ providedIn: 'root' })
export class SettingService {
    private http = inject(HttpClient);

    getUserGroups(): Observable<any> {
        return this.http.get(`${API_URL}/user-groups`);
    }
}
