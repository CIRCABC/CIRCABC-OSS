import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { SummaryBoxComponent } from './summary-box.component';

describe('SummaryBoxComponent', () => {
  let fixture: ComponentFixture<SummaryBoxComponent>;
  let componentRef: ComponentRef<SummaryBoxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SummaryBoxComponent],
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

    fixture = TestBed.createComponent(SummaryBoxComponent);
    componentRef = fixture.componentRef;
    componentRef.setInput('number', 5);
    componentRef.setInput('label', 'uploads');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should display the number', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.content-top')?.textContent?.trim()).toBe('5');
  });

  it('should show upload image when label is uploads', () => {
    const img = fixture.nativeElement.querySelector(
      '.big-icon img'
    ) as HTMLImageElement;
    expect(img.src).toContain('upload-white-64.png');
  });

  it('should show update image when label is updates', () => {
    componentRef.setInput('label', 'updates');
    fixture.detectChanges();
    const img = fixture.nativeElement.querySelector(
      '.big-icon img'
    ) as HTMLImageElement;
    expect(img.src).toContain('update-white-64.png');
  });

  it('should show comments image when label is comments', () => {
    componentRef.setInput('label', 'comments');
    fixture.detectChanges();
    const img = fixture.nativeElement.querySelector(
      '.big-icon img'
    ) as HTMLImageElement;
    expect(img.src).toContain('chat-white-64.png');
  });
});
