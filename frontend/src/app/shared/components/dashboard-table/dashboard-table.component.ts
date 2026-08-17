import { Component, EventEmitter, Input, Output } from '@angular/core';
import {COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS} from '../../providers/shared-providers';

@Component({
  selector: 'app-dashboard-table',
  imports: [COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS],
  templateUrl: './dashboard-table.component.html',
  styleUrl: './dashboard-table.component.scss'
})
export class DashboardTableComponent {

    @Input() data: any[] = [];
    @Input() isLoading = false;
    @Input() pagination: any;
    @Input() searchText = '';
    @Input() columns: any[] = [];
    @Input() searchPlaceholder = 'Search...';
    @Input() hasActions = false;

    @Output() search = new EventEmitter<void>();
    @Output() reset = new EventEmitter<void>();
    @Output() pageChange = new EventEmitter<any>();
    @Output() searchTextChange = new EventEmitter<string>();
    @Output() rowClick = new EventEmitter<any>();
    @Output() approve = new EventEmitter<number>();
    @Output() disapprove = new EventEmitter<number>();

    currentPage = 1;

    onPageChange(): void {
        this.pageChange.emit({
            pageIndex: this.currentPage - 1,
            pageSize: this.pagination.pageSize
        });
    }

    onRowClick(row: any): void {
        if (this.rowClick.observed) {
            this.rowClick.emit(row);
        }
    }

    getCellValue(row: any, col: any): any {
        if (col.formatter) {
            return typeof col.formatter === 'function' ? col.formatter(row) : row[col.key];
        }

        // Handle nested properties like 'tellerOR.id'
        if (col.key.includes('.')) {
            return col.key.split('.').reduce((obj: any, key: string) => obj?.[key], row);
        }

        return row[col.key];
    }
}
