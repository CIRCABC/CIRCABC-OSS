import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SpinnerComponent } from './spinner.component';

describe('SpinnerComponent', () => {
  let fixture: ComponentFixture<SpinnerComponent>;
  let component: SpinnerComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SpinnerComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SpinnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should default white to false', () => {
    expect(component.white()).toBe(false);
  });

  it('should default float to right', () => {
    expect(component.float()).toBe('right');
  });

  it('should apply white-speeding-wheel class when white is true', () => {
    fixture.componentRef.setInput('white', true);
    fixture.detectChanges();

    const wheel = fixture.nativeElement.querySelector(
      '.cssload-speeding-wheel'
    );
    expect(wheel.classList.contains('white-speeding-wheel')).toBe(true);
  });

  it('should not apply white-speeding-wheel class when white is false', () => {
    const wheel = fixture.nativeElement.querySelector(
      '.cssload-speeding-wheel'
    );
    expect(wheel.classList.contains('white-speeding-wheel')).toBe(false);
  });

  it('should set float style on container', () => {
    fixture.componentRef.setInput('float', 'left');
    fixture.detectChanges();

    const container = fixture.nativeElement.querySelector('.cssload-container');
    expect(container.style.float).toBe('left');
  });
});
