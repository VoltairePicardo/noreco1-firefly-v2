import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddCheckModalComponent } from './add-check-modal.component';

describe('AddCheckModalComponent', () => {
  let component: AddCheckModalComponent;
  let fixture: ComponentFixture<AddCheckModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddCheckModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddCheckModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
