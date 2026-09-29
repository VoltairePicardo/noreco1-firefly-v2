import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { COMMON_ALL_PAGE_IMPORTS } from '@/app/shared/providers/shared-providers';
import { SharedModule } from '@/app/shared/shared.module';
import { AlertService } from '@/app/shared/services/alert.service';
import { provideIcons } from '@ng-icons/core';
import { tablerPrinter } from '@ng-icons/tabler-icons';
import { MeterTestingService } from '../meter-testing.service';
import { MeterTestingData, MeterTestingOptionDetailGroup } from '@/app/models/special-equipment-testing/meter-testing.model';

@Component({
    selector: 'app-meter-testing-detail',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, SharedModule],
    templateUrl: './meter-testing-detail.component.html',
    providers: [provideIcons({ tablerPrinter })]
})
export class MeterTestingDetailComponent implements OnInit {
    module    = 'Meter Testing';
    subModule = 'Details';
    menuLink  = 'meter-testing';

    data = signal<MeterTestingData | null>(null);
    isLoading = signal(false);

    optionDetailGroups = computed<MeterTestingOptionDetailGroup[]>(() => {
        const groups: MeterTestingOptionDetailGroup[] = [];
        const groupByTypeId = new Map<number, MeterTestingOptionDetailGroup>();

        for (const optionDetail of this.data()?.optionDetails ?? []) {
            const typeId = optionDetail.meterTestingOption?.optionType?.id ?? 0;
            let group = groupByTypeId.get(typeId);
            if (!group) {
                group = { typeId, typeDescription: optionDetail.meterTestingOption?.optionType?.description ?? '', options: [] };
                groupByTypeId.set(typeId, group);
                groups.push(group);
            }
            group.options.push(optionDetail);
        }

        return groups;
    });

    isIndividual = computed(() => this.data()?.details?.length === 1);

    private route        = inject(ActivatedRoute);
    private router       = inject(Router);
    private service      = inject(MeterTestingService);
    private alertService = inject(AlertService);

    print(): void {
        const id = this.data()?.id;
        if (id != null) { this.service.print(id); }
    }

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            const idParam = params.get('id');
            const id = idParam != null && /^\d+$/.test(idParam) ? Number(idParam) : null;

            if (id !== null) {
                this.isLoading.set(true);
                this.service.getData(id).subscribe({
                    next: (data) => {
                        this.isLoading.set(false);
                        if (data?.id) { this.data.set(data); }
                        else { this.alertService.error(this.module, 'Not Found', ''); this.router.navigate(['/' + this.menuLink]); }
                    },
                    error: () => { this.isLoading.set(false); this.alertService.error(this.module, 'Error', ''); this.router.navigate(['/' + this.menuLink]); }
                });
            }
        });
    }
}
