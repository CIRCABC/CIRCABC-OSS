import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { BigNumberComponent } from './big-number.component';

describe('BigNumberComponent', () => {
  let fixture: ComponentFixture<BigNumberComponent>;
  let componentRef: ComponentRef<BigNumberComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BigNumberComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: {
            getTranslation: vi
              .fn()
              .mockReturnValue(of({ 'test.label': 'Test Label' })),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BigNumberComponent);
    componentRef = fixture.componentRef;
    componentRef.setInput('value', 42);
    componentRef.setInput('label', 'test.label');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should display the value', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.value')?.textContent?.trim()).toBe('42');
  });

  it('should display the translated label', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.label')?.textContent?.trim()).toBe('Test Label');
  });

  it('should update when inputs change', () => {
    componentRef.setInput('value', 100);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.value')?.textContent?.trim()).toBe('100');
  });
});
