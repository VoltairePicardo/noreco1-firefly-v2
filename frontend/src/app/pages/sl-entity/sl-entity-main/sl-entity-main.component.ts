import { Component, inject, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { SlEntityService } from '../sl-entity.service';
import Swal from 'sweetalert2';

@Component({
    selector: 'app-sl-entity-main',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS],
    templateUrl: './sl-entity-main.component.html'
})
export class SlEntityMainComponent {
    module    = 'SL Entity';
    subModule = '';
    menuLink  = 'sl-entity';

    entities           = signal<any[]>([]);
    classifications    = signal<any[]>([]);
    isLoading          = signal(false);
    pageNumber         = signal(0);
    totalPages         = signal(0);
    totalElements      = signal(0);
    pageSize           = 10;
    searchText         = '';
    selectedClassification = '';

    private service      = inject(SlEntityService);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.loadClassifications();
        this.load();
    }

    loadClassifications(): void {
        this.service.getClassifications().subscribe({
            next: (data) => this.classifications.set(data),
            error: () => {}
        });
    }

    load(page = 0): void {
        this.isLoading.set(true);
        this.service.list(this.searchText, this.selectedClassification, page).subscribe({
            next: (data) => {
                this.entities.set(data.content);
                this.pageNumber.set(data.page.number);
                this.totalPages.set(data.page.totalPages);
                this.totalElements.set(data.page.totalElements);
                this.isLoading.set(false);
            },
            error: () => { this.alertService.error(this.module, 'Load', ''); this.isLoading.set(false); }
        });
    }

    search():      void { this.load(0); }
    clearSearch(): void { this.searchText = ''; this.selectedClassification = ''; this.load(0); }
    onPageChange(p: number): void { this.load(p - 1); }

    delete(id: number, name: string): void {
        Swal.fire({
            title: 'Delete SL Entity?',
            text: `Are you sure you want to delete "${name}"?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Yes, delete',
            confirmButtonColor: '#d33',
            cancelButtonText: 'Cancel'
        }).then(result => {
            if (result.isConfirmed) {
                this.service.remove(id).subscribe({
                    next: (res) => {
                        if (res.success) {
                            Swal.fire({ title: 'Deleted!', text: res.successMessage, icon: 'success', timer: 2000, showConfirmButton: false });
                            this.load(this.pageNumber());
                        } else {
                            Swal.fire({ title: 'Error', text: res.failureMessage, icon: 'error' });
                        }
                    },
                    error: () => Swal.fire({ title: 'Error', text: 'Failed to delete SL Entity.', icon: 'error' })
                });
            }
        });
    }
}
