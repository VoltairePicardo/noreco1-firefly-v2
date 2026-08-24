import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { AlertService } from '@/app/shared/services/alert.service';
import { ProjectAcceptanceReportService } from '../project-acceptance-report.service';
import { monthStart, monthEnd } from '@/app/shared/utils/date.utils';

@Component({
    selector: 'app-project-acceptance-report-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrModule],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults],
    templateUrl: './project-acceptance-report-main.component.html'
})
export class ProjectAcceptanceReportMainComponent {
    module    = 'Project Acceptance Report';
    subModule = '';
    menuLink  = 'project-acceptance-report';

    records       = signal<any[]>([]);
    isLoading     = signal(false);
    pageNumber    = signal(0);
    totalElements = signal(0);
    pageSize = 10;

    query    = '';
    fromDate = '';
    toDate   = '';
    selectedStatusId = signal<number | null>(null);
    documentStatuses = signal<any[]>([]);

    flatpickrOptions = { dateFormat: 'Y-m-d', altInput: true, altFormat: 'F j, Y' };

    private service      = inject(ProjectAcceptanceReportService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.loadStatuses();
        this.load();
    }

    setDefaultDates(): void {
        this.fromDate = monthStart();
        this.toDate   = monthEnd();
    }

    loadStatuses(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.documentStatuses.set(data || []),
            error: () => {}
        });
    }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.list(this.query, this.fromDate, this.toDate, this.selectedStatusId(), page, this.pageSize).subscribe({
            next: (data) => {
                this.records.set(data?.content ?? []);
                this.pageNumber.set(data?.page?.number ?? 0);
                this.totalElements.set(data?.page?.totalElements ?? 0);
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(this.module, 'Load', '');
                this.isLoading.set(false);
            }
        });
    }

    search(): void { this.load(0); }

    reset(): void {
        this.query = '';
        this.selectedStatusId.set(null);
        this.setDefaultDates();
        this.load(0);
    }

    onPageChange(p: number): void { this.load(p - 1); }
}
