import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HorizontalLoaderComponent } from './horizontal-loader.component';

describe('HorizontalLoaderComponent', () => {
  let component: HorizontalLoaderComponent;
  let fixture: ComponentFixture<HorizontalLoaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HorizontalLoaderComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(HorizontalLoaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should render a loader div', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.loader')).toBeDefined();
  });
});
