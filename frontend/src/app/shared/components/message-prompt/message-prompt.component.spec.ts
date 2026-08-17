import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MessagePromptComponent } from './message-prompt.component';

describe('MessagePromptComponent', () => {
  let component: MessagePromptComponent;
  let fixture: ComponentFixture<MessagePromptComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MessagePromptComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MessagePromptComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
