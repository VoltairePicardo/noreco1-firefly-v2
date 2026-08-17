import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerSearch } from '@ng-icons/tabler-icons';
import { ProjectService } from '@/app/pages/project/project.service';

@Component({
    selector: 'app-browse-project-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerSearch })],
    templateUrl: './browse-project-modal.component.html'
})
export class BrowseProjectModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private service = inject(ProjectService);
    private cdr = inject(ChangeDetectorRef);

    projects: any[] = [];
    loading = false;
    searchText = '';

    ngOnInit(): void {
        this.loading = true;
        this.service.list('', '', '', null, 0, 200).subscribe({
            next: (data) => {
                this.projects = data?.content ?? [];
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
        if (!q) return this.projects;
        return this.projects.filter(p =>
            p.code?.toLowerCase().includes(q) ||
            p.name?.toLowerCase().includes(q) ||
            p.location?.toLowerCase().includes(q)
        );
    }

    select(project: any): void {
        this.activeModal.close({ action: 'select', data: project });
    }
}
