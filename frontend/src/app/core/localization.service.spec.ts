import { TestBed } from '@angular/core/testing';
import { TranslocoService } from '@jsverse/transloco';
import { LocalizationService } from 'app/core/localization.service';
import { vi } from 'vitest';

describe('LocalizationService', () => {
  let service: LocalizationService;
  let mockTransloco: {
    getActiveLang: ReturnType<typeof vi.fn>;
    translateObject: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockTransloco = {
      getActiveLang: vi.fn().mockReturnValue('en'),
      translateObject: vi.fn().mockReturnValue({
        dayNames: [
          'Sunday',
          'Monday',
          'Tuesday',
          'Wednesday',
          'Thursday',
          'Friday',
          'Saturday',
        ],
        dayNamesShort: ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'],
        monthNames: [
          'January',
          'February',
          'March',
          'April',
          'May',
          'June',
          'July',
          'August',
          'September',
          'October',
          'November',
          'December',
        ],
      }),
    };

    TestBed.configureTestingModule({
      providers: [
        LocalizationService,
        { provide: TranslocoService, useValue: mockTransloco },
      ],
    });

    service = TestBed.inject(LocalizationService);
  });

  describe('getDayNames', () => {
    it('should return 7 days starting from Sunday by default', () => {
      const result = service.getDayNames();
      expect(Object.keys(result)).toHaveLength(7);
      expect(result[0]).toBe('Sunday');
      expect(result[6]).toBe('Saturday');
    });

    it('should return 7 days starting from Monday when specified', () => {
      const result = service.getDayNames('Monday');
      expect(result[0]).toBe('Monday');
      expect(result[6]).toBe('Sunday');
    });

    it('should return short day names', () => {
      const result = service.getDayNames('Sunday', 'short');
      expect(result[0]).toBe('Sun');
      expect(result[6]).toBe('Sat');
    });

    it('should return 5 days when numberOfDays is 5', () => {
      const result = service.getDayNames('Sunday', 'full', 5);
      expect(Object.keys(result)).toHaveLength(5);
      expect(result[4]).toBe('Thursday');
    });

    it('should cache results on subsequent calls', () => {
      service.getDayNames();
      service.getDayNames();
      // translateObject called once for first call, cached for second
      expect(mockTransloco.translateObject).toHaveBeenCalledTimes(1);
    });

    it('should return fallback day names when translateObject returns undefined', () => {
      mockTransloco.translateObject.mockReturnValue({});
      const result = service.getDayNames();
      expect(result[0]).toBe('Sunday');
    });

    it('should not cache fallback results', () => {
      mockTransloco.translateObject.mockReturnValue({});
      service.getDayNames();
      service.getDayNames();
      expect(mockTransloco.translateObject).toHaveBeenCalledTimes(2);
    });
  });

  describe('getMonthsNames', () => {
    it('should return 12 months indexed from 1', () => {
      const result = service.getMonthsNames();
      expect(result[1]).toBe('January');
      expect(result[12]).toBe('December');
    });

    it('should cache results on subsequent calls', () => {
      service.getMonthsNames();
      service.getMonthsNames();
      expect(mockTransloco.translateObject).toHaveBeenCalledTimes(1);
    });

    it('should return fallback month names when translateObject returns undefined', () => {
      mockTransloco.translateObject.mockReturnValue({});
      const result = service.getMonthsNames();
      expect(result[1]).toBe('January');
      expect(result[12]).toBe('December');
    });
  });

  describe('getFirstDayOfWeek', () => {
    it('should return 0 for Portuguese locale', () => {
      mockTransloco.getActiveLang.mockReturnValue('pt');
      expect(service.getFirstDayOfWeek()).toBe(0);
    });

    it('should return 1 for other locales', () => {
      mockTransloco.getActiveLang.mockReturnValue('en');
      expect(service.getFirstDayOfWeek()).toBe(1);
    });
  });
});
