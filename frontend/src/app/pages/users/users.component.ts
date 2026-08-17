import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { AuthService } from '@/app/pages/auth/auth.service';
import { environment } from '@/environments/environment';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';

const API_URL = environment.get('baseApiUrl') + '/user';

@Component({
    selector: 'app-users',
    imports: [CommonModule, FormsModule],
    templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {

    private http = inject(HttpClient);
    private authService = inject(AuthService);

    users: any[] = [];
    total = 0;
    page = 0;
    size = 10;
    searchText = '';
    loading = false;
    error = '';

    private search$ = new Subject<string>();

    ngOnInit(): void {
        this.search$.pipe(
            debounceTime(350),
            distinctUntilChanged(),
            switchMap(q => {
                this.page = 0;
                return this.fetchUsers(q);
            })
        ).subscribe({
            next: res => this.handleResponse(res),
            error: () => this.error = 'Failed to load users.'
        });

        this.load();
    }

    load(): void {
        this.loading = true;
        this.error = '';
        this.fetchUsers(this.searchText).subscribe({
            next: res => this.handleResponse(res),
            error: () => {
                this.error = 'Failed to load users.';
                this.loading = false;
            }
        });
    }

    private fetchUsers(q: string) {
        const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });
        const params = new HttpParams()
            .set('searchText', q)
            .set('page', this.page)
            .set('size', this.size);
        return this.http.get<any>(`${API_URL}/pageable`, { headers, params });
    }

    private handleResponse(res: any): void {
        this.users = res.content ?? [];
        this.total = res.totalElements ?? 0;
        this.loading = false;
    }

    onSearch(): void {
        this.search$.next(this.searchText);
    }

    goToPage(p: number): void {
        if (p < 0 || p >= this.totalPages) return;
        this.page = p;
        this.load();
    }

    get totalPages(): number {
        return Math.ceil(this.total / this.size);
    }

    get pages(): number[] {
        return Array.from({ length: this.totalPages }, (_, i) => i);
    }
}
