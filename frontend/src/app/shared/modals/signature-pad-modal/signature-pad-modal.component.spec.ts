import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SignaturePadModalComponent } from './signature-pad-modal.component';

describe('SignaturePadModalComponent', () => {
  let component: SignaturePadModalComponent;
  let fixture: ComponentFixture<SignaturePadModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SignaturePadModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SignaturePadModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
