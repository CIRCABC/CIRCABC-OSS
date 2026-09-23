import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { FileDetailsComponent } from './file-details.component';

describe('FileDetailsComponent', () => {
  let fixture: ComponentFixture<FileDetailsComponent>;
  let componentRef: ComponentRef<FileDetailsComponent>;

  const mockFile = new File(['content'], 'test.pdf', {
    type: 'application/pdf',
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileDetailsComponent],
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

    fixture = TestBed.createComponent(FileDetailsComponent);
    componentRef = fixture.componentRef;
    componentRef.setInput('file', mockFile);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeDefined();
  });

  it.each([
    ['file name', 'test.pdf'],
    ['file type', 'application/pdf'],
    ['file size', '7 B'],
  ])('should display the %s', (_label, expected) => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain(expected);
  });
});
