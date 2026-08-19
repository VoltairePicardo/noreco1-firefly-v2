import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerPhoto, tablerFile, tablerExternalLink, tablerCamera, tablerList } from '@ng-icons/tabler-icons';
import { CheckReleasingService } from '../check-releasing.service';

@Component({
    selector: 'app-check-releasing-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerPhoto, tablerFile, tablerExternalLink, tablerCamera, tablerList })],
    templateUrl: './check-releasing-detail.component.html'
})
export class CheckReleasingDetailComponent {
    module    = 'Check Releasing';
    subModule = 'Detail';
    menuLink  = 'check-releasing';

    id: any   = 0;
    data: any = {};
    files     = signal<any[]>([]);
    isLoading = signal(false);

    showLogs     = false;
    logs         = signal<any[]>([]);
    logsLoading  = signal(false);

    private service      = inject(CheckReleasingService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id && /^\d+$/.test(String(this.id))) {
                this.loadData();
            }
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).subscribe({
            next: (data) => {
                this.isLoading.set(false);
                if (data?.id) {
                    this.data = data;
                    this.loadFiles();
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    loadFiles(): void {
        this.service.getFiles(this.id).subscribe({
            next: (files) => this.files.set(Array.isArray(files) ? files : []),
            error: () => {}
        });
    }

    isImage(mimeType: string): boolean {
        return /^image\//i.test(mimeType || '');
    }

    fileUrl(fileId: number): string {
        return this.service.downloadFileUrl(fileId);
    }

    toggleLogs(): void {
        this.showLogs = !this.showLogs;
        if (this.showLogs && this.logs().length === 0) {
            this.loadLogs();
        }
    }

    loadLogs(): void {
        const transId = this.data?.transaction?.id;
        if (!transId) return;
        this.logsLoading.set(true);
        this.service.getLogs(transId).subscribe({
            next: (logs) => {
                this.logs.set(Array.isArray(logs) ? logs : []);
                this.logsLoading.set(false);
            },
            error: () => this.logsLoading.set(false)
        });
    }

    parseLogValue(newValue: string): any {
        try { return JSON.parse(newValue); } catch { return {}; }
    }
}
