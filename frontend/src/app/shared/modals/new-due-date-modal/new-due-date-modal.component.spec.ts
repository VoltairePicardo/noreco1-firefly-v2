import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewDueDateModalComponent } from './new-due-date-modal.component';

describe('NewDueDateModalComponent', () => {
  let component: NewDueDateModalComponent;
  let fixture: ComponentFixture<NewDueDateModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewDueDateModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewDueDateModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
