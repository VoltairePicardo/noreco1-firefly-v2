import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnterRemarksModalComponent } from './enter-remarks-modal.component';

describe('EnterRemarksModalComponent', () => {
  let component: EnterRemarksModalComponent;
  let fixture: ComponentFixture<EnterRemarksModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnterRemarksModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnterRemarksModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
