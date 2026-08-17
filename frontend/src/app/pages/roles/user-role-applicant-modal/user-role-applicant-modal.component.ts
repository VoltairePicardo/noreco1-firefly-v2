import { Component, inject, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { RolesService } from '../roles.service';

@Component({
    selector: 'app-user-role-applicant-modal',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    templateUrl: './user-role-applicant-modal.component.html'
})
export class UserRoleApplicantModalComponent {

    @Input() roleId: number = 0;

    loading    = false;
    dataLists: any[] = [];
    selected: any[]  = [];
    total  = 0;
    page   = 1;
    size   = 10;
    searchText = '';

    private service     = inject(RolesService);
    public  activeModal = inject(NgbActiveModal);

    ngOnInit(): void {
        this.getData();
    }

    getData(): void {
        this.loading = true;
        this.service.getUsersForApplication(this.roleId, this.searchText, this.page - 1, this.size).subscribe({
            next: (data) => { this.dataLists = data.content; this.total = data.totalElements; this.loading = false; },
            error: ()     => { this.loading = false; }
        });
    }

    search(): void     { this.page = 1; this.getData(); }
    clearSearch(): void { this.searchText = ''; this.page = 1; this.getData(); }
    onPageChange(p: number): void { this.page = p; this.getData(); }

    isSelected(user: any): boolean {
        return this.selected.some(u => u.id === user.id);
    }

    toggle(user: any, event: Event): void {
        if ((event.target as HTMLInputElement).checked) {
            if (!this.isSelected(user)) this.selected.push(user);
        } else {
            this.selected = this.selected.filter(u => u.id !== user.id);
        }
    }

    confirm(): void { this.activeModal.close(this.selected); }
    close(): void   { this.activeModal.dismiss(); }
}
