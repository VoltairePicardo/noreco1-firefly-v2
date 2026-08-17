import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RabImageViewModalComponent } from './rab-image-view-modal.component';

describe('RabImageViewModalComponent', () => {
  let component: RabImageViewModalComponent;
  let fixture: ComponentFixture<RabImageViewModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RabImageViewModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RabImageViewModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
