import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddMiscellaneousItemModalComponent } from './add-miscellaneous-item-modal.component';

describe('AddMiscellaneousItemModalComponent', () => {
  let component: AddMiscellaneousItemModalComponent;
  let fixture: ComponentFixture<AddMiscellaneousItemModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddMiscellaneousItemModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddMiscellaneousItemModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
