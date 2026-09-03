import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '@/app/pages/auth/auth.service';
import { Router } from '@angular/router';
import { environment } from '@/environments/environment';

const BASE_API = environment.get('baseApiUrl');

@Component({
    selector: 'app-home',
    imports: [CommonModule],
    templateUrl: './home.component.html',
    styleUrl: './home.component.scss'
})
export class HomeComponent {

    private authService = inject(AuthService);
    private router = inject(Router);
    private http = inject(HttpClient);

    user: any = {};
    activeTab: 'documents' | 'other-approved' = 'documents';
    rows = signal<any[]>([]);
    isLoading = signal(false);

    ngOnInit(): void {
        if (!this.authService.isLoggedIn()) {
            this.router.navigate(['/login']);
            return;
        }
        this.user = this.authService.getUser();
        this.loadDocuments();
    }

    selectTab(tab: 'documents' | 'other-approved'): void {
        this.activeTab = tab;
        if (tab === 'documents') {
            this.loadDocuments();
        } else {
            this.loadOtherApproved();
        }
    }

    private loadDocuments(): void {
        this.isLoading.set(true);
        this.http.get<any[]>(`${BASE_API}/dashboard/documents`).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.rows.set([]); this.isLoading.set(false); }
        });
    }

    private loadOtherApproved(): void {
        this.isLoading.set(true);
        this.http.get<any[]>(`${BASE_API}/dashboard/other-approved-documents`).subscribe({
            next: (data) => { this.rows.set(data ?? []); this.isLoading.set(false); },
            error: () => { this.rows.set([]); this.isLoading.set(false); }
        });
    }
}
