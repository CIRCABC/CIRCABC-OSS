import { TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { vi } from 'vitest';

import { TimePipe } from './time.pipe';

describe('TimePipe', () => {
  let pipe: TimePipe;
  const mockTransloco = { getActiveLang: vi.fn().mockReturnValue('en') };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TimePipe,
        { provide: TranslocoService, useValue: mockTransloco },
      ],
    });
    pipe = TestBed.inject(TimePipe);
  });

  it('should return empty string when date is undefined', () => {
    expect(pipe.transform(undefined, '10:00', 'GMT')).toBe('');
  });

  it('should return empty string when time is undefined', () => {
    expect(pipe.transform('2026-01-15', undefined, 'GMT')).toBe('');
  });

  it('should return empty string when timeZone is undefined', () => {
    expect(pipe.transform('2026-01-15', '10:00', undefined)).toBe('');
  });

  it('should return a locale time string for valid inputs', () => {
    const result = pipe.transform('2026-01-15', '10:00', 'GMT');
    expect(result).toBeDefined();
    expect(result).not.toBe('');
  });

  it('should use the active language from TranslocoService', () => {
    pipe.transform('2026-01-15', '10:00', 'GMT');
    expect(mockTransloco.getActiveLang).toHaveBeenCalled();
  });

  it('should handle different timezone offsets', () => {
    const resultGmt = pipe.transform('2026-01-15', '10:00', 'GMT');
    const resultGmtPlus5 = pipe.transform('2026-01-15', '10:00', 'GMT+5');
    expect(resultGmt).not.toBe('');
    expect(resultGmtPlus5).not.toBe('');
  });
});
