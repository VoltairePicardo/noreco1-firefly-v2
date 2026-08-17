import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '@/app/pages/auth/auth.service';
import { environment } from '@/environments/environment';

const API_URL = environment.get('baseApiUrl') + '/accounting/accounts';

@Component({
    selector: 'app-coa',
    imports: [CommonModule, FormsModule],
    templateUrl: './coa.component.html'
})
export class CoaComponent implements OnInit {

    private http = inject(HttpClient);
    private authService = inject(AuthService);

    accounts: any[] = [];
    filtered: any[] = [];
    searchText = '';
    loading = false;
    error = '';

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading = true;
        this.error = '';
        const headers = new HttpHeaders({ Authorization: `Bearer ${this.authService.getToken()}` });
        this.http.get<any[]>(`${API_URL}/list`, { headers }).subscribe({
            next: data => {
                this.accounts = data;
                this.applyFilter();
                this.loading = false;
            },
            error: () => {
                this.error = 'Failed to load chart of accounts.';
                this.loading = false;
            }
        });
    }

    applyFilter(): void {
        const q = this.searchText.trim().toLowerCase();
        if (!q) {
            this.filtered = this.accounts;
            return;
        }
        this.filtered = this.accounts.filter(a =>
            (a.code && a.code.toLowerCase().includes(q)) ||
            (a.title && a.title.toLowerCase().includes(q)) ||
            (a.classification && a.classification.toLowerCase().includes(q)) ||
            (a.accountType?.description && a.accountType.description.toLowerCase().includes(q))
        );
    }

    normalBalanceLabel(nb: number): string {
        return nb === 1 ? 'Debit' : nb === 2 ? 'Credit' : '—';
    }
}
