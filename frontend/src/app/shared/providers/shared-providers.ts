import {TableService} from '@/app/shared/services/table.service';
import {AsyncPipe, CommonModule, DecimalPipe} from '@angular/common';
import {LucideAngularModule} from 'lucide-angular';
import {NgIcon, NgIconComponent} from '@ng-icons/core';
import {RouterLink} from '@angular/router';
import {NgbdSortableHeader} from '@core/directive/sortable.directive';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
    NgbDropdownModule,
    NgbPagination,
    NgbPaginationNext,
    NgbPaginationPrevious,
    NgbTooltip
} from '@ng-bootstrap/ng-bootstrap';
import {PageTitleComponent} from '@app/components/page-title.component';
import {DefaultTableComponent} from '@/app/shared/tables/default-table/default-table.component';
import {UiCardComponent} from '@app/components/ui-card.component';
import {LaddaModule} from 'angular2-ladda';
import {ChoiceSelectInputDirective} from '@core/directive/choices-select.directive';
import {SharedModule} from '@/app/shared/shared.module';
import {SweetAlert2Module} from '@sweetalert2/ngx-sweetalert2';
import {ReportFilterContainerComponent} from '@/app/shared/report-filters/components/report-filter-container.component';
import {DateRangeFilterComponent} from '@/app/shared/report-filters/components/filters/date-range.filter';
import {CutOffDateFilterComponent} from '@/app/shared/report-filters/components/filters/cut-off-date.filter';
import {ReportTypeFilterComponent} from '@/app/shared/report-filters/components/filters/report-type.filter';
import {PrintOptionFilterComponent} from '@/app/shared/report-filters/components/filters/print-option.filter';
import {DateOptionFilterComponent} from '@/app/shared/report-filters/components/filters/date-option.filter';
import {AgeFilterComponent} from '@/app/shared/report-filters/components/filters/age.filter';
import {BillOptionFilterComponent} from '@/app/shared/report-filters/components/filters/bill-option.filter';

export const SHARED_PROVIDERS = [TableService, DecimalPipe];
export const COMMON_ALL_PAGE_IMPORTS = [
    CommonModule,
    LucideAngularModule,
    NgIcon,
    NgIconComponent,
    RouterLink,
    PageTitleComponent,
    DefaultTableComponent,
    UiCardComponent,
];
export const COMMON_ADD_EDIT_PAGE_IMPORTS = [
    FormsModule,
    ReactiveFormsModule,
    LaddaModule,
    ChoiceSelectInputDirective,
];
export const COMMON_MAIN_PAGE_IMPORTS = [
    NgbdSortableHeader,
    FormsModule,
    NgbPagination,
    NgbPaginationNext,
    NgbPaginationPrevious,
    AsyncPipe,
    NgbDropdownModule,
    NgbTooltip,
    DecimalPipe,
];

export const COMMON_MAIN_PAGE_PROVIDERS = [TableService, DecimalPipe];
export const COMMON_REPORT_FILTERS_IMPORTS = [
    ReportFilterContainerComponent,
    DateRangeFilterComponent,
    CutOffDateFilterComponent,
    ReportTypeFilterComponent,
    PrintOptionFilterComponent,
    DateOptionFilterComponent,
    AgeFilterComponent,
    BillOptionFilterComponent,
];
