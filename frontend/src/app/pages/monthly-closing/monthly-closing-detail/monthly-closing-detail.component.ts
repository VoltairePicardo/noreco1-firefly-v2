import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { MonthlyClosingService } from '../monthly-closing.service';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerEdit, tablerEye, tablerEyeOff } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-monthly-closing-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, RouterLink],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerEdit, tablerEye, tablerEyeOff })],
    templateUrl: './monthly-closing-detail.component.html'
})
export class MonthlyClosingDetailComponent {
    module    = 'Monthly Closing';
    subModule = 'Details';
    menuLink  = 'monthly-closing';
    id: any   = 0;
    data: any = {};
    isLoading   = signal(false);
    logs        : any[] = [];
    showLogs    = false;
    logsLoading = false;

    private service      = inject(MonthlyClosingService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) { this.loadData(); }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) { this.data = data; }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    formatMonth(year: number, month: number): string {
        if (!year || !month) return '—';
        return new Date(year, month - 1, 1).toLocaleString('default', { month: 'long', year: 'numeric' });
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs.length === 0 && !this.logsLoading) { this.loadLogs(); }
    }

    loadLogs(): void {
        if (this.logsLoading) return;
        this.logsLoading = true;
        this.service.getLogs(this.id).subscribe({
            next: (logs) => { this.logs = logs || []; this.logsLoading = false; },
            error: () => { this.logsLoading = false; }
        });
    }
}
