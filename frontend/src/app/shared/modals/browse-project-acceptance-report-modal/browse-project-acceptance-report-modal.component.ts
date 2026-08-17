import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { ProjectAcceptanceReportService } from '@/app/pages/project-acceptance-report/project-acceptance-report.service';

@Component({
    selector: 'app-browse-project-acceptance-report-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-project-acceptance-report-modal.component.html'
})
export class BrowseProjectAcceptanceReportModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(ProjectAcceptanceReportService);
    private cdr = inject(ChangeDetectorRef);

    reports: any[] = [];
    loading = false;
    searchText = '';

    ngOnInit(): void {
        this.loading = true;
        this.service.list('', '', '', null, 0, 200).subscribe({
            next: (data) => {
                this.reports = data?.content ?? [];
                this.loading = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.loading = false;
                this.cdr.markForCheck();
            }
        });
    }

    get filtered(): any[] {
        const q = this.searchText.toLowerCase();
        if (!q) return this.reports;
        return this.reports.filter(r => r.code?.toLowerCase().includes(q));
    }

    select(report: any): void {
        this.activeModal.close({ action: 'select', data: report });
    }
}
