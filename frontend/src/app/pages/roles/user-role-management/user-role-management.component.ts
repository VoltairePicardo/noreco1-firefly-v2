import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AlertService } from '@/app/shared/services/alert.service';
import { SharedModalService } from '@/app/shared/modals/shared-modal-service/shared-modal.service';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { RolesService } from '../roles.service';
import { UserRoleApplicantModalComponent } from '../user-role-applicant-modal/user-role-applicant-modal.component';

@Component({
    selector: 'app-user-role-management',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    templateUrl: './user-role-management.component.html'
})
export class UserRoleManagementComponent {

    module    = 'Role';
    menuLink  = 'roles';
    id        = 0;
    role: any = {};

    userRoles: any[] = [];
    pendingUsers: any[] = [];

    total    = 0;
    page     = 1;
    pageSize = 8;
    searchText = '';

    isLoading = signal(false);

    private service      = inject(RolesService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);
    private modalService = inject(SharedModalService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            this.id = idParam ? parseInt(idParam) : 0;
            this.loadRole();
            this.loadUsers();
        });
    }

    loadRole(): void {
        this.service.getData(this.id).subscribe({
            next: (data) => {
                if (data?.id) { this.role = data; }
                else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
            },
            error: () => { this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
        });
    }

    loadUsers(): void {
        this.isLoading.set(true);
        this.service.getUsersByRoleId(this.id, this.searchText, this.page - 1, this.pageSize).subscribe({
            next: (data) => { this.userRoles = data.content; this.total = data.totalElements; this.isLoading.set(false); },
            error: ()     => { this.isLoading.set(false); this.alertService.error(this.module, 'Load Users', ''); }
        });
    }

    search(): void      { this.page = 1; this.loadUsers(); }
    clearSearch(): void { this.searchText = ''; this.page = 1; this.loadUsers(); }
    onPageChange(p: number): void { this.page = p; this.loadUsers(); }

    async openModal(): Promise<void> {
        const result = await this.modalService.openModal(
            UserRoleApplicantModalComponent,
            { roleId: this.id },
            { size: 'xl', centered: true }
        ).catch(() => null);

        if (result && result.length > 0) {
            this.pendingUsers = result;
        }
    }

    removePending(index: number): void {
        this.pendingUsers.splice(index, 1);
    }

    assignUsers(): void {
        if (!this.pendingUsers.length) return;
        this.service.assignUsersToRole(this.pendingUsers, this.id).subscribe({
            next: (res) => {
                if (res.success) {
                    this.alertService.success(this.module, 'Assigned', res.successMessage);
                    this.pendingUsers = [];
                    this.loadUsers();
                } else {
                    this.alertService.error(this.module, 'Assign', res.failureMessage);
                }
            },
            error: () => { this.alertService.error(this.module, 'Assign', ''); }
        });
    }

    deleteUserRole(user: any): void {
        this.alertService.confirm('This will remove the user from this role.').then((result: any) => {
            if (result.isConfirmed) {
                this.service.deleteUserRole(user.id, this.id).subscribe({
                    next: (res) => {
                        if (res.success) {
                            this.alertService.success(this.module, 'Removed', res.successMessage);
                            this.loadUsers();
                        } else {
                            this.alertService.error(this.module, 'Remove', res.failureMessage);
                        }
                    },
                    error: () => { this.alertService.error(this.module, 'Remove', ''); }
                });
            }
        });
    }
}
