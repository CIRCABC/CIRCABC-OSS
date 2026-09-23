import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { of } from 'rxjs';
import { vi } from 'vitest';
import {
  SummarizeDialogComponent,
  SummarizeDialogData,
} from './summarize-dialog.component';

describe('SummarizeDialogComponent', () => {
  const mockDialogRef = { close: vi.fn() };
  const mockData: SummarizeDialogData = {
    nodeName: 'test.pdf',
    loading: signal(false),
    summary: signal('This is a summary.'),
    error: signal(''),
  };

  beforeEach(() => {
    mockDialogRef.close.mockClear();
    TestBed.configureTestingModule({
      imports: [SummarizeDialogComponent],
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef },
        { provide: MAT_DIALOG_DATA, useValue: mockData },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(SummarizeDialogComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeDefined();
  });

  it('should inject dialog data', () => {
    const fixture = TestBed.createComponent(SummarizeDialogComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.data).toBe(mockData);
  });

  it('should close dialog when close is called', () => {
    const fixture = TestBed.createComponent(SummarizeDialogComponent);
    fixture.detectChanges();
    fixture.componentInstance.dialogRef.close();
    expect(mockDialogRef.close).toHaveBeenCalled();
  });
});
