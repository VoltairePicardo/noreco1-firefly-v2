import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');
const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class RolesService {
    private http = inject(HttpClient);

    list(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/role/list`);
    }

    getData(id: number): Observable<any> {
        return this.http.get(`${BASE_API}/role/${id}`);
    }

    create(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/role/create`, form, httpOptions);
    }

    update(form: any): Observable<any> {
        return this.http.post(`${BASE_API}/role/update`, form, httpOptions);
    }

    getMenus(): Observable<any[]> {
        return this.http.get<any[]>(`${BASE_API}/json/menus`);
    }

    getUsersByRoleId(id: number, query: string, pageIndex: number, pageSize: number): Observable<any> {
        return this.http.get(`${BASE_API}/role/user-role-paged?id=${id}&query=${query}&pageIndex=${pageIndex}&pageSize=${pageSize}`);
    }

    getUsersForApplication(id: number, query: string, pageIndex: number, pageSize: number): Observable<any> {
        return this.http.get(`${BASE_API}/role/apply-user-role?id=${id}&query=${query}&pageIndex=${pageIndex}&pageSize=${pageSize}`);
    }

    assignUsersToRole(users: any[], roleId: number): Observable<any> {
        return this.http.post(`${BASE_API}/role/create-via-user-role-management?roleId=${roleId}`, users, httpOptions);
    }

    deleteUserRole(userId: number, roleId: number): Observable<any> {
        return this.http.post(`${BASE_API}/role/delete-user-role`, { userId, roleId }, httpOptions);
    }
}
