import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { AlertService } from '@/app/shared/services/alert.service';
import { AccountSettingService } from '../account-setting.service';
import { DOC_TYPE_OPTIONS } from '../account-setting-add-edit/account-setting-add-edit.component';
import { forkJoin, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { provideIcons } from '@ng-icons/core';
import { tablerArrowLeft, tablerEdit } from '@ng-icons/tabler-icons';

@Component({
    selector: 'app-account-setting-detail',
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS, RouterLink],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerArrowLeft, tablerEdit })],
    templateUrl: './account-setting-detail.component.html'
})
export class AccountSettingDetailComponent {
    module   = 'Account Setting';
    menuLink = 'account-setting';
    id: any  = null;

    isLoading   = signal(true);
    header: any = null;
    details: any[] = [];
    rrLinkedData: any = null;

    docTypeOptions = DOC_TYPE_OPTIONS;

    private service      = inject(AccountSettingService);
    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private alertService = inject(AlertService);

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.id = params.get('id');
            if (this.id) this.loadData();
        });
    }

    loadData(): void {
        this.isLoading.set(true);
        this.service.getData(this.id).pipe(
            switchMap(header => {
                const details$ = this.service.getDetails(this.id);
                const rrLinked$ = header?.receivingReport?.id
                    ? this.service.getRrLinkedDetails(header.receivingReport.id)
                    : of(null);
                return forkJoin({ header: of(header), details: details$, rrLinked: rrLinked$ });
            })
        ).subscribe({
            next: ({ header, details, rrLinked }) => {
                this.isLoading.set(false);
                if (header?.id) {
                    this.header       = header;
                    this.details      = details || [];
                    this.rrLinkedData = rrLinked;
                } else {
                    this.alertService.error(this.module, 'Not Found', '');
                    this.router.navigate(['/' + this.menuLink]);
                }
            },
            error: () => {
                this.isLoading.set(false);
                this.alertService.error(this.module, 'Error', '');
                this.router.navigate(['/' + this.menuLink]);
            }
        });
    }

    getLinkedDoc(): any {
        if (!this.header) return null;
        for (const opt of DOC_TYPE_OPTIONS) {
            const doc = this.header[opt.field];
            if (doc) return { ...doc, typeLabel: opt.label };
        }
        return null;
    }

    accountLabel(account: any): string {
        if (!account) return '—';
        return `${account.code || ''} — ${account.title || ''}`.trim();
    }
}
