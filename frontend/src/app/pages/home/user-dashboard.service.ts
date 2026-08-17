import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {PaginatedResult} from '@/app/pages/auth/auth.service';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';

import {environment} from '@/environments/environment';
const API_URL = environment.get('baseApiUrl') + '/user-dashboard';

const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class UserDashboardService {

    constructor(private http: HttpClient) { }

    getPendingCollectionCancellation(query = '', page:number, size: number  ): Observable<any> {
        return this.http.get(`${API_URL}/pending-collection-cancellation?query=${query}&page=${page}&size=${size}`, httpOptions);
    }

    getPendingDisconnectionOrder(query = '', page:number, size: number  ): Observable<any> {
        return this.http.get(`${API_URL}/pending-disconnection-orders?query=${query}&page=${page}&size=${size}`, httpOptions);
    }

    getPendingMeterReplacement(query = '', page:number, size: number  ): Observable<any> {
        return this.http.get(`${API_URL}/pending-meter-replacement?query=${query}&page=${page}&size=${size}`, httpOptions);
    }

    getPendingApplicationList(query = '', page:number, size: number  ): Observable<any> {
        return this.http.get(`${API_URL}/pending-application-list?query=${query}&page=${page}&size=${size}`, httpOptions);
    }

    getPendingComplaintList(query = '', page:number, size: number  ): Observable<any> {
        return this.http.get(`${API_URL}/pending-complaint-list?query=${query}&page=${page}&size=${size}`, httpOptions);
    }

    getPendingJobOrderList(query = '', page:number, size: number  ): Observable<any> {
        return this.http.get(`${API_URL}/pending-job-order-list?query=${query}&page=${page}&size=${size}`, httpOptions);
    }

    getPendingTurnOnOrderList(query = '', page:number, size: number  ): Observable<any> {
        return this.http.get(`${API_URL}/pending-turn-on-order-list?query=${query}&page=${page}&size=${size}`, httpOptions);
    }

    getPendingApprehensionList(query = '', page:number, size: number  ): Observable<any> {
        return this.http.get(`${API_URL}/pending-apprehension-list?query=${query}&page=${page}&size=${size}`, httpOptions);
    }

    getPendingTurnOnOrder(): Observable<any> {
        return this.http.get(API_URL + '/pending-turn-on-order');
    }

    getPendingJobOrder(): Observable<any> {
        return this.http.get(API_URL + '/pending-job-order');
    }

    getPendingComplaint(): Observable<any> {
        return this.http.get(API_URL + '/pending-complaint');
    }

    getPendingApprehension(): Observable<any> {
        return this.http.get(API_URL + '/pending-apprehension');
    }

    isUserTellerSupervisor(): Observable<any> {
        return this.http.get(API_URL + '/is-user-teller-supervisor');
    }

    isUserAuthorizedMeterReplacement(): Observable<any> {
        return this.http.get(API_URL + '/is-user-authorized-meter-replacement');
    }

    isUserAuthorizedDiscoAccomplishment(): Observable<any> {
        return this.http.get(API_URL + '/is-user-authorized-disco-accomplishment');
    }

    isUserAuthorizedForApplicationAndAssessment(): Observable<any> {
        return this.http.get(API_URL + '/is-user-authorized-application-assessment');
    }

    isUserAuthorizedForApplicationChecking(): Observable<any> {
        return this.http.get(API_URL + '/is-user-authorized-application-checking');
    }

    isUserAuthorizedForMembership(): Observable<any> {
        return this.http.get(API_URL + '/is-user-authorized-membership');
    }

    isUserAuthorizedForTurnOnOrder(): Observable<any> {
        return this.http.get(API_URL + '/is-user-authorized-turn-on-order');
    }

    isUserAuthorizedForJobOrder(): Observable<any> {
        return this.http.get(API_URL + '/is-user-authorized-job-order');
    }

}
