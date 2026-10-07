import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { BudgetLineItemService } from '../budget-line-item.service';
import Swal from 'sweetalert2';
import { AuthService } from '../../auth/auth.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-budget-line-item-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './budget-line-item-main.component.html'
})
export class BudgetLineItemMainComponent {
    module    = 'Budget Line Item';
    subModule = '';
    menuLink  = 'budget-line-item';

    records          = signal<any[]>([]);
    departments      = signal<any[]>([]);
    divisions        = signal<any[]>([]);
    documentStatuses = signal<any[]>([]);
    isLoading        = signal(false);
    approvingAll     = false;

    yearFilter: number | null = new Date().getFullYear();

    selectedDepartment: any = null;
    selectedDivision: any = null;
    selectedStatus: any = null;

    defaultDepartment: any = null;
    defaultDivision: any = null;
    isLoadingFilters = signal(true);

    searchQuery = '';
    page = 1;
    pageSize = 20;

    private authService = inject(AuthService);
    private service = inject(BudgetLineItemService);
    private alertService = inject(AlertService);
    private router = inject(Router);

    get filteredRecords(): any[] {
        const q = this.searchQuery.toLowerCase();

        return this.records().filter(r =>
            !q ||
            (r.code || '').toLowerCase().includes(q) ||
            (r.department?.name || '').toLowerCase().includes(q) ||
            (r.division?.name || r.division?.abbreviation || '').toLowerCase().includes(q)
        );
    }

    get pagedRecords(): any[] {
        const start = (this.page - 1) * this.pageSize;
        return this.filteredRecords.slice(start, start + this.pageSize);
    }

    get grandTotal(): number {
        return this.records().reduce((sum, r) => {
            return sum + (r.budgetLineItemDetails || []).reduce(
                (s: number, d: any) => s + (d.totalPrice || 0),
                0
            );
        }, 0);
    }

    ngOnInit(): void {
        if (!this.authService.isLoggedIn()) {
            this.router.navigate(['/login']);
            return;
        }

        this.initialize();
    }

    initialize(): void {
        this.isLoadingFilters.set(true);

        this.service.getDepartments().subscribe({
            next: (departments) => {
                this.departments.set(departments || []);

                this.service.getDivisions().subscribe({
                    next: (divisions) => {
                        this.divisions.set(divisions || []);

                        this.service.getUserDepartment().subscribe({
                            next: (department) => {
                                this.defaultDepartment = department || null;
                                this.selectedDepartment = this.defaultDepartment;

                                this.service.getUserDivision().subscribe({
                                    next: (division) => {
                                        this.defaultDivision = division || null;
                                        this.selectedDivision = this.defaultDivision;
                                        this.isLoadingFilters.set(false);
                                        this.load();
                                    },
                                    error: () => {
                                        this.defaultDivision = null;
                                        this.selectedDivision = null;
                                        this.isLoadingFilters.set(false);
                                        this.load();
                                    }
                                });
                            },
                            error: () => {
                                this.defaultDepartment = null;
                                this.selectedDepartment = null;

                                this.service.getUserDivision().subscribe({
                                    next: (division) => {
                                        this.defaultDivision = division || null;
                                        this.selectedDivision = this.defaultDivision;
                                        this.isLoadingFilters.set(false);
                                        this.load();
                                    },
                                    error: () => {
                                        this.defaultDivision = null;
                                        this.selectedDivision = null;
                                        this.isLoadingFilters.set(false);
                                        this.load();
                                    }
                                });
                            }
                        });
                    },
                    error: () => {
                        this.divisions.set([]);
                        this.isLoadingFilters.set(false);
                    }
                });
            },
            error: () => {
                this.departments.set([]);
                this.isLoadingFilters.set(false);
            }
        });

        this.service.getDocumentStatuses().subscribe({
            next: (d) => this.documentStatuses.set(d || []),
            error: () => {}
        });
    }

    load(): void {
        this.isLoading.set(true);
        this.records.set([]);
        this.page = 1;

        this.service.list(
            this.yearFilter || undefined,
            this.selectedDepartment?.id || undefined,
            this.selectedDivision?.id || undefined,
            this.selectedStatus?.id || undefined
        ).subscribe({
            next: (data) => {
                this.records.set(data || []);
                this.page = 1;
                this.isLoading.set(false);
            },
            error: () => {
                this.alertService.error(
                    this.module,
                    'Failed to load records.',
                    ''
                );
                this.isLoading.set(false);
            }
        });
    }

    reset(): void {
        this.yearFilter = new Date().getFullYear();

        this.selectedDepartment = this.defaultDepartment || null;
        this.selectedDivision = this.defaultDivision || null;

        this.selectedStatus = null;
        this.searchQuery = '';
        this.page = 1;

        this.load();
    }

    isEditable(rec: any): boolean {
        const s = rec?.documentStatus?.status || rec?.status || '';

        return s === 'Document Created' || s === 'Returned to Creator';
    }

    compareById(a: any, b: any): boolean {
        return a && b ? a.id === b.id : a === b;
    }

    approveAll(): void {
        const toApprove = this.records().filter(r => {
            const s = r?.documentStatus?.status || r?.status || '';

            return s !== 'Approved' && s !== 'Cancelled';
        });

        if (toApprove.length === 0) {
            this.alertService.error(
                this.module,
                'No pending records to approve.',
                ''
            );

            return;
        }

        Swal.fire({
            title: 'Approve All?',
            text: `Approve all ${toApprove.length} pending Budget Line Item(s)?`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Yes, approve all',
            confirmButtonColor: '#0d6efd'
        }).then(result => {
            if (!result.isConfirmed) return;

            this.approvingAll = true;

            const payload = toApprove.map(r => ({
                documentId: r.id,
                remarks: '',
                documentType: 'BUDGET_LINE_ITEM'
            }));

            this.service.approveAll(payload).subscribe({
                next: (res) => {
                    this.approvingAll = false;

                    if (res?.success) {
                        this.alertService.success(
                            this.module,
                            res.successMessage || 'All items approved.',
                            ''
                        );

                        this.load();
                    } else {
                        this.alertService.error(
                            this.module,
                            res?.failureMessage || 'Approve all failed.',
                            ''
                        );
                    }
                },
                error: () => {
                    this.approvingAll = false;

                    this.alertService.error(
                        this.module,
                        'An error occurred.',
                        ''
                    );
                }
            });
        });
    }

    delete(rec: any): void {
        Swal.fire({
            title: 'Delete?',
            text: `Delete ${rec.code}?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes, delete',
            confirmButtonColor: '#d33'
        }).then(result => {
            if (!result.isConfirmed) return;

            this.service.delete(rec.id).subscribe({
                next: (res) => {
                    if (res?.success) {
                        this.alertService.success(
                            this.module,
                            'Deleted successfully.',
                            ''
                        );

                        this.load();
                    } else {
                        this.alertService.error(
                            this.module,
                            res?.failureMessage || 'Delete failed.',
                            ''
                        );
                    }
                },
                error: () => {
                    this.alertService.error(
                        this.module,
                        'An error occurred.',
                        ''
                    );
                }
            });
        });
    }

    get filteredDivisions(): any[] {
        if (!this.selectedDepartment) {
            return this.divisions();
        }

        return this.divisions().filter(d =>
            d.departmentId === this.selectedDepartment.id ||
            d.department?.id === this.selectedDepartment.id
        );
    }

    onDepartmentChange(): void {
        if (
            this.selectedDivision &&
            !this.filteredDivisions.some(
                d => d.id === this.selectedDivision.id
            )
        ) {
            this.selectedDivision = null;
        }
    }
}