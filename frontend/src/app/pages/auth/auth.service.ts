import {inject, Injectable, signal} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from '@angular/common/http';
import { Router } from '@angular/router';
import {tap, catchError, throwError, Observable, of} from 'rxjs';
import { environment } from '@/environments/environment';
import {map} from 'rxjs/operators';

const AUTH_API = environment.get('baseApiUrl') + '/auth';
const USER_API = environment.get('baseApiUrl') + '/user';

export interface PaginatedResult<> {
    items: any[];
    total: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

    menus = signal<any[]>([]);
    constructor(private http: HttpClient, private router: Router) {}
    login(credentials: { username: string; password: string }) {
        return this.http.post<{ token: string; user: any }>(AUTH_API + '/signin', credentials)
            .pipe(
                tap(res => {
                    localStorage.setItem('token', res.token);
                    localStorage.setItem('user', JSON.stringify(res));
                }),
                catchError(this.handleError)
            );
    }

    fetchAndStoreMenus(): Observable<any[]> {
        const headers = new HttpHeaders({ Authorization: `Bearer ${this.getToken() ?? ''}` });
        return this.http.get<any[]>(environment.get('baseApiUrl') + '/menus', { headers }).pipe(
            tap(menus => {
                localStorage.setItem('menus', JSON.stringify(menus));
            }),
            catchError((err) => {
                console.error('[Menus] API error:', err);
                localStorage.setItem('menus', '[]');
                return of([]);
            })
        );
    }

    getMenus(): any[] {
        try {
            return JSON.parse(localStorage.getItem('menus') || '[]');
        } catch {
            return [];
        }
    }

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('menus');
        this.router.navigate(['/login']);
    }

    isLoggedIn(): boolean {
        return !!localStorage.getItem('token') && !!localStorage.getItem('user');
    }

    getToken(): string | null {
        return localStorage.getItem('token');
    }

    public getUser() {
        return JSON.parse(localStorage.getItem('user')!);
    }

    private handleError(error: HttpErrorResponse) {
        let msg = 'Something went wrong';
        if (error.status === 401) msg = 'Invalid username or password';
        else if (error.status === 0) msg = 'Cannot connect to server';
        return throwError(() => new Error(msg));
    }

    menuIsAllowed(route: string): boolean {
        for (const menu of this.getUser().menus) {
           if(route === menu.link || (menu.parentMenu != null && menu.parentMenu.link === route)){
               return true;
           }
        }
        return false;
    }

    getPagedData(searchText: string, page: number, size: number): Observable<PaginatedResult> {
        const params = new HttpParams()
            .set('searchText', searchText || '')
            .set('page', page)
            .set('size', size);

        return this.http.get<any>(USER_API + '/pageable', {params}).pipe(
            map(response => ({
                items: response.content,        // <-- MAP THIS
                total: response.totalElements   // <-- MAP THIS
            }))
        );
    }
}
