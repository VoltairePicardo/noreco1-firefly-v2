import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { FlatpickrDefaults, FlatpickrModule } from 'angularx-flatpickr';
import { AlertService } from '@/app/shared/services/alert.service';
import { ProjectAcceptanceCertificationService } from '../project-acceptance-certification.service';

@Component({
    selector: 'app-project-acceptance-certification-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, FlatpickrModule],
    providers: [...SHARED_PROVIDERS, FlatpickrDefaults],
    templateUrl: './project-acceptance-certification-main.component.html'
})
export class ProjectAcceptanceCertificationMainComponent {
    module    = 'Project Acceptance Certification';
    subModule = '';
    menuLink  = 'project-acceptance-certification';

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

    private service      = inject(ProjectAcceptanceCertificationService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.setDefaultDates();
        this.loadStatuses();
        this.load();
    }

    setDefaultDates(): void {
        const now   = new Date();
        const first = new Date(now.getFullYear(), now.getMonth(), 1);
        const last  = new Date(now.getFullYear(), now.getMonth() + 1, 0);
        this.fromDate = first.toISOString().substring(0, 10);
        this.toDate   = last.toISOString().substring(0, 10);
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
