import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { PermissionDescriptorComponent } from './permission-descriptor.component';

describe('PermissionDescriptorComponent', () => {
  let component: PermissionDescriptorComponent;
  let fixture: ComponentFixture<PermissionDescriptorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PermissionDescriptorComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PermissionDescriptorComponent);
    fixture.componentRef.setInput('label', 'test.label');
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set mustExpand to true when content overflows', () => {
    const el = component.elementView().nativeElement;
    Object.defineProperty(el, 'offsetWidth', {
      value: 100,
      configurable: true,
    });
    Object.defineProperty(el, 'scrollWidth', {
      value: 200,
      configurable: true,
    });

    component.toggleExpand();

    expect(component.mustExpand).toBe(true);
  });

  it('should set mustExpand to false when content does not overflow', () => {
    component.mustExpand = true;
    const el = component.elementView().nativeElement;
    Object.defineProperty(el, 'offsetWidth', {
      value: 200,
      configurable: true,
    });
    Object.defineProperty(el, 'scrollWidth', {
      value: 100,
      configurable: true,
    });

    component.toggleExpand();

    expect(component.mustExpand).toBe(false);
  });
});
