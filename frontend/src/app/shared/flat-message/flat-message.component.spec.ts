import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FlatMessageComponent } from './flat-message.component';

describe('FlatMessageComponent', () => {
  let fixture: ComponentFixture<FlatMessageComponent>;
  let component: FlatMessageComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FlatMessageComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FlatMessageComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('message', 'Test message');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the message', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Test message');
  });

  it('should update when message input changes', () => {
    fixture.componentRef.setInput('message', 'Updated message');
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Updated message');
  });
});
