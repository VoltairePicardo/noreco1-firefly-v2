import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { tablerCheck, tablerX, tablerList } from '@ng-icons/tabler-icons';
import Swal from 'sweetalert2';

import { ItemService } from '@/app/pages/item/service/item.service';

@Component({
  selector: 'app-add-miscellaneous-item-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NgIconComponent
  ],
  providers: [
    // NgbActiveModal,
    provideIcons({ tablerCheck, tablerX, tablerList })
  ],
  templateUrl: './add-miscellaneous-item-modal.component.html',
  styleUrls: ['./add-miscellaneous-item-modal.component.scss']
})
export class AddMiscellaneousItemModalComponent implements OnInit {
  private readonly itemService = inject(ItemService);
  public readonly activeModal = inject(NgbActiveModal);

  @Input() data: any;

  selectedItem: any = 0;
  public items: any[] = [];
  public defaultAmount: number = 0;

  item: any = {
    id: 0,
    description: '',
    amount: 0,
    salesAccount: 0,
    receivablesAccount: 0,
    vatable: false
  };

  ngOnInit(): void {
    if (this.data?.items) {
      this.items = this.data.items;
    }
    this.initObjects();
  }

  initObjects(): void {}

  itemChanged(event: any): void {
    const selectedId = parseInt(event.target.value);
    const selectedItem = this.items.find((item: any) => item.id === selectedId);

    if (selectedItem) {
      this.item = {
        id: selectedItem.id,
        description: selectedItem.description,
        amount: selectedItem.amount || 0,
        salesAccount: selectedItem.salesAccount || 0,
        receivablesAccount: selectedItem.receivablesAccount || 0,
        vatable: selectedItem.vatable || false
      };
    }
  }

  closeDialog(): void {
    this.activeModal.dismiss();
  }

  addItem(): void {
    if (!this.item.id) {
      Swal.fire('', 'Please select an item.', 'warning');
      return;
    }
    this.activeModal.close(this.item);
  }
}
