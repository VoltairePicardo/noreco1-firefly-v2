import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { COMMON_ALL_PAGE_IMPORTS, COMMON_MAIN_PAGE_IMPORTS, SHARED_PROVIDERS } from '@/app/shared/providers/shared-providers';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideIcons } from '@ng-icons/core';
import { tablerChevronLeft, tablerChevronRight, tablerSearch } from '@ng-icons/tabler-icons';
import { BrowseConsumerModalService } from './browse-consumer-modal.service';
import { ConsumerMeterPage, ConsumerMeterRow, ConsumerMeterSelection } from './browse-consumer-modal.model';

@Component({
    selector: 'app-browse-consumer-modal',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [...COMMON_ALL_PAGE_IMPORTS, ...COMMON_MAIN_PAGE_IMPORTS],
    providers: [...SHARED_PROVIDERS, provideIcons({ tablerChevronLeft, tablerChevronRight, tablerSearch })],
    templateUrl: './browse-consumer-modal.component.html'
})
export class BrowseConsumerModalComponent implements OnInit {
    activeModal = inject(NgbActiveModal);
    private consumerModalService = inject(BrowseConsumerModalService);

    items = signal<ConsumerMeterRow[]>([]);
    total = signal(0);
    page = signal(1);
    pageSize = signal(10);
    searchText = signal('');
    loading = signal(false);

    hasResults = computed(() => this.total() > 0);

    ngOnInit(): void {
        // this.loadData();
    }

    loadData(): void {
        if (this.loading()) return;
        this.loading.set(true);
        this.consumerModalService.list(this.searchText(), this.page() - 1, this.pageSize()).subscribe({
            next: (res) => this.handleResponse(res),
            error: () => this.loading.set(false)
        });
    }

    onSearchChange(): void {
        this.page.set(1);
        this.loadData();
    }

    select(consumerMeter: ConsumerMeterRow): void {
        const selection: ConsumerMeterSelection = {
            consumerId: consumerMeter.consumer?.id ?? null,
            accountNo: consumerMeter.consumer?.accountNo ?? null,
            accountName: consumerMeter.consumer?.accountName ?? null,
            oldAccountNo: consumerMeter.consumer?.oldAccountNo ?? null,
            address: consumerMeter.consumer?.address ?? null,
            meterId: consumerMeter.meter?.id ?? null,
            meterSerialNo: consumerMeter.meter?.serialNo ?? null,
            presentReading: consumerMeter.meter?.presentReading ?? null
        };
        this.activeModal.close({ action: 'select', data: selection });
    }

    private handleResponse(res: ConsumerMeterPage): void {
        const content = res.content ?? [];
        this.items.set(content);
        this.total.set(res.totalElements ?? res.page?.totalElements ?? content.length);
        this.loading.set(false);
    }
}
