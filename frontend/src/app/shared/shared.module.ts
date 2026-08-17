import {ModuleWithProviders, NgModule} from '@angular/core';
import {AsyncPipe, CommonModule, DecimalPipe} from '@angular/common';
import {LucideAngularModule} from "lucide-angular";
import {NgIcon} from "@ng-icons/core";
import {RouterLink} from "@angular/router";
import {NgbdSortableHeader} from "@core/directive/sortable.directive";
import {FormsModule} from "@angular/forms";
import {NgbPagination, NgbPaginationNext, NgbPaginationPrevious} from "@ng-bootstrap/ng-bootstrap";
import {PageTitleComponent} from "@app/components/page-title.component";
import {DefaultTableComponent} from "@/app/shared/tables/default-table/default-table.component";

@NgModule({
  imports: [
    CommonModule,
    LucideAngularModule,
    NgIcon,
    RouterLink,
    NgbdSortableHeader,
    FormsModule,
    NgbPagination,
    NgbPaginationNext,
    NgbPaginationPrevious,
    AsyncPipe,
    PageTitleComponent,
    DefaultTableComponent
  ],
  exports: [
    CommonModule,
    LucideAngularModule,
    NgIcon,
    RouterLink,
    NgbdSortableHeader,
    FormsModule,
    NgbPagination,
    NgbPaginationNext,
    NgbPaginationPrevious,
    AsyncPipe,
    PageTitleComponent,
    DefaultTableComponent
  ]
})
export class SharedModule {
  static forRoot(): ModuleWithProviders<SharedModule> {
    return {
      ngModule: SharedModule,
    };
  }
}
