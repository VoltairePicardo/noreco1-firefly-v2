import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { WorkOrderService } from '../work-order.service';

const MONTH_NAMES = ['', 'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'];

@Component({
    selector: 'app-work-order-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './work-order-main.component.html'
})
export class WorkOrderMainComponent {
    module    = 'Work Order';
    subModule = '';
    menuLink  = 'work-order';

    records       = signal<any[]>([]);
    isLoading     = signal(false);
    pageNumber    = signal(0);
    totalElements = signal(0);
    pageSize = 10;

    selectedStatusId = signal<number | null>(null);
    selectedYear     = signal<number | null>(null);
    selectedMonth    = signal<number | null>(null);
    documentStatuses = signal<any[]>([]);

    searchText = '';

    years: number[] = [];
    months = MONTH_NAMES.slice(1).map((name, i) => ({ id: i + 1, name }));

    private service      = inject(WorkOrderService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        const currentYear = new Date().getFullYear();
        for (let y = currentYear - 5; y <= currentYear + 1; y++) this.years.push(y);
        this.selectedYear.set(currentYear);
        this.selectedMonth.set(new Date().getMonth() + 1);
        this.loadStatuses();
        this.load();
    }

    loadStatuses(): void {
        this.service.getDocumentStatuses().subscribe({
            next: (data) => this.documentStatuses.set(data || []),
            error: () => {}
        });
    }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.list(this.selectedStatusId(), this.selectedYear(), this.selectedMonth(), page, this.pageSize, this.searchText).subscribe({
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
        this.selectedStatusId.set(null);
        const currentYear = new Date().getFullYear();
        this.selectedYear.set(currentYear);
        this.selectedMonth.set(new Date().getMonth() + 1);
        this.searchText = '';
        this.load(0);
    }

    onPageChange(p: number): void { this.load(p - 1); }

    typeLabel(type: string): string {
        if (type === 'MATERIALS') return 'Materials';
        if (type === 'LABOR')     return 'Labor';
        if (type === 'BOTH')      return 'Materials & Labor';
        return type || '—';
    }

    getMonthName(id: number): string { return MONTH_NAMES[id] || ''; }
}
