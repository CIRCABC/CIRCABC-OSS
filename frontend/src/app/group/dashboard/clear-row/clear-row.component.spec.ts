import { TestBed } from '@angular/core/testing';
import { ClearRowComponent } from './clear-row.component';

describe('ClearRowComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClearRowComponent],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(ClearRowComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should render a div with class clear', () => {
    const fixture = TestBed.createComponent(ClearRowComponent);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.clear')).toBeDefined();
  });
});
