import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {HttpHeaders} from "@angular/common/http";
import {environment} from '@/environments/environment';

const API_URL = environment.get('baseApiUrl') + '/auth/profile';
const USER_API = environment.get('baseApiUrl') + '/user';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) { }

  getProfile(id:any): Observable<any> {
    return this.http.get(API_URL + '/' +id);
  }

  updateProfile(user:any): Observable<any> {
    return this.http.post(API_URL + 'update', user, httpOptions);
  }

    create(account:any): Observable<any> {
        return this.http.post(API_URL + '/create', account, httpOptions);
    }

    update(account:any): Observable<any> {
        return this.http.post(API_URL + '/update', account, httpOptions);
    }

    roles(): Observable<any> {
        return this.http.get(API_URL + '/roles');
    }

    readerList(): Observable<any> {
        return this.http.get(API_URL + '/readers');
    }

    crewList(): Observable<any> {
        return this.http.get(API_URL + '/crews');
    }

    activePagedList(searchText:any, groupId:number, pageIndex:number, pageSize:number): Observable<any> {
        return this.http.get(API_URL + '/active-list?searchText='+searchText+"&groupId="+groupId+"&pageIndex="+pageIndex+"&pageSize="+pageSize);
    }

    listForDiscoCrewDropDown(): Observable<any>{
        return this.http.get(API_URL + '/disconnection-crew-dropdown-list');
    }

    getAllUser(): Observable<any> {
        return this.http.get(API_URL + '/all-users');
    }

    getAllCollectors(): Observable<any> {
        return this.http.get(API_URL + '/all-collectors');
    }

    getAllMysqlUsers(searchText:number, pageIndex:number, pageSize:number): Observable<any> {
        return this.http.get(API_URL + '/get-all-mysql-users-pageable?searchText='+searchText+"&pageIndex="+pageIndex+"&pageSize="+pageSize);
    }

    getAllMenu(): Observable<any> {
        return this.http.get(API_URL + '/all-menu');
    }

    // user logs for accessed modules
    accessedModule(payload:any): Observable<any> {
        return this.http.post(API_URL + '/module-accessed', payload, httpOptions);
    }

    getApprovingOfficers(): Observable<any> {
        return this.http.get(API_URL + '/approving-officers');
    }

    getAllUsersByRole(roleId:number): Observable<any> {
        return this.http.get(API_URL + '/find-by-role/' + roleId);
    }

    getAllUsersByRoles(roleIds: string): Observable<any> {
        return this.http.get(API_URL + '/find-by-roles/' + roleIds);
    }

    changePassword(payload: { oldPassword: string; newPassword: string }): Observable<any> {
        return this.http.post(USER_API + '/change-password', payload, httpOptions);
    }

    getSelfProfile(): Observable<any> {
        return this.http.get(USER_API + '/profile');
    }

    updateSelfProfile(payload: any): Observable<any> {
        return this.http.post(USER_API + '/update-profile', payload, httpOptions);
    }

}
