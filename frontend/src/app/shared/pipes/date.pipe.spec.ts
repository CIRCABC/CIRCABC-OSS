import { TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { vi } from 'vitest';

import { DatePipe } from './date.pipe';

describe('DatePipe', () => {
  let pipe: DatePipe;
  const mockTranslocoService = {
    getActiveLang: vi.fn().mockReturnValue('en'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DatePipe,
        { provide: TranslocoService, useValue: mockTranslocoService },
      ],
    });
    pipe = TestBed.inject(DatePipe);
  });

  it('should return empty string when date is undefined', () => {
    expect(pipe.transform(undefined, '10:00', 'GMT')).toBe('');
  });

  it('should return empty string when time is undefined', () => {
    expect(pipe.transform('2026-04-30', undefined, 'GMT')).toBe('');
  });

  it('should return empty string when timeZone is undefined', () => {
    expect(pipe.transform('2026-04-30', '10:00', undefined)).toBe('');
  });

  it('should format date using en-GB locale when active lang is en', () => {
    mockTranslocoService.getActiveLang.mockReturnValue('en');
    const result = pipe.transform('2026-04-30', '10:00', 'GMT');
    expect(result).toBe(
      new Date('2026-04-30T10:00:00.000+00:00').toLocaleDateString('en-GB')
    );
  });

  it('should format date using active lang locale when not en', () => {
    mockTranslocoService.getActiveLang.mockReturnValue('fr');
    const result = pipe.transform('2026-04-30', '10:00', 'GMT+1');
    expect(result).toBe(
      new Date('2026-04-30T10:00:00.000+01:00').toLocaleDateString('fr')
    );
  });

  it('should handle negative timezone offsets', () => {
    mockTranslocoService.getActiveLang.mockReturnValue('en');
    const result = pipe.transform('2026-04-30', '02:00', 'GMT-5');
    expect(result).toBe(
      new Date('2026-04-30T02:00:00.000-05:00').toLocaleDateString('en-GB')
    );
  });
});
