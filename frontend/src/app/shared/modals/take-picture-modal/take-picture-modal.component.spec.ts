import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TakePictureModalComponent } from './take-picture-modal.component';

describe('TakePictureModalComponent', () => {
  let component: TakePictureModalComponent;
  let fixture: ComponentFixture<TakePictureModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TakePictureModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TakePictureModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
