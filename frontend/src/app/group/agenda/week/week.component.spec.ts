import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  EventItemDefinition,
  EventsService,
  UserService,
} from 'app/core/generated/circabc';
import { LocalizationService } from 'app/core/localization.service';
import { LoginService } from 'app/core/login.service';
import { TimeZoneHelperService } from 'app/core/timezone-helper.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { WeekComponent } from './week.component';

const mockDayNames: { [key: string]: string } = {
  0: 'Monday',
  1: 'Tuesday',
  2: 'Wednesday',
  3: 'Thursday',
  4: 'Friday',
  5: 'Saturday',
  6: 'Sunday',
};

const mockEvent: EventItemDefinition = {
  id: 'event-1',
  appointmentType: 'Event',
  appointmentDate: '2026-04-27',
  startTime: '09:00',
  endTime: '10:00',
  title: 'Test Event',
  timeZone: 'Europe/Brussels',
};

const mockMeeting: EventItemDefinition = {
  id: 'meeting-1',
  appointmentType: 'Meeting',
  appointmentDate: '2026-04-28',
  startTime: '14:00',
  endTime: '15:30',
  title: 'Test Meeting',
  timeZone: 'Europe/Brussels',
};

describe('WeekComponent', () => {
  let component: WeekComponent;
  let componentRef: ComponentRef<WeekComponent>;
  let fixture: ComponentFixture<WeekComponent>;

  const mockEventsService = {
    getInterestGroupEventsAsync: vi
      .fn()
      .mockResolvedValue([mockEvent, mockMeeting]),
  };

  const mockUserService = {
    getUserEventsPeriodAsync: vi.fn().mockResolvedValue([mockEvent]),
  };

  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('testuser'),
  };

  const mockLocalizationService = {
    getDayNames: vi.fn().mockReturnValue(mockDayNames),
  };

  const mockTimeZoneHelperService = {
    toLocalDateTime: vi
      .fn()
      .mockImplementation((events: EventItemDefinition[]) => events),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WeekComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: EventsService, useValue: mockEventsService },
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: LocalizationService, useValue: mockLocalizationService },
        { provide: TimeZoneHelperService, useValue: mockTimeZoneHelperService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(WeekComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;

    // Set required inputs
    componentRef.setInput('date', new Date(2026, 3, 27)); // Monday April 27, 2026
    componentRef.setInput('displayFromHour', 8);
    componentRef.setInput('displayToHour', 18);
    componentRef.setInput('workWeek', false);
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnChanges', () => {
    it('should load events for the interest group when id is set', async () => {
      componentRef.setInput('id', 'ig-123');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockEventsService.getInterestGroupEventsAsync).toHaveBeenCalled();
      expect(component.eventsPerWeekDays).toBeDefined();
      expect(component.eventsPerWeekDays).toHaveLength(7);
    });

    it('should load user events when meMode is true', async () => {
      componentRef.setInput('meMode', true);
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockUserService.getUserEventsPeriodAsync).toHaveBeenCalled();
      expect(mockLoginService.getCurrentUsername).toHaveBeenCalled();
    });

    it('should compute weekDates with 7 days', async () => {
      componentRef.setInput('id', 'ig-123');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.weekDates).toHaveLength(7);
    });
  });

  describe('isWeekendDay', () => {
    it('should return true for Saturday (5) and Sunday (6)', () => {
      expect(component.isWeekendDay(5)).toBe(true);
      expect(component.isWeekendDay(6)).toBe(true);
    });

    it('should return false for weekdays', () => {
      expect(component.isWeekendDay(0)).toBe(false);
      expect(component.isWeekendDay(4)).toBe(false);
    });
  });

  describe('isEvent / isMeeting', () => {
    it('should identify an event', () => {
      expect(component.isEvent(mockEvent)).toBe(true);
      expect(component.isMeeting(mockEvent)).toBe(false);
    });

    it('should identify a meeting', () => {
      expect(component.isMeeting(mockMeeting)).toBe(true);
      expect(component.isEvent(mockMeeting)).toBe(false);
    });
  });

  describe('isOnDate', () => {
    it('should return true when event is on the given day and hour', async () => {
      componentRef.setInput('id', 'ig-123');
      fixture.detectChanges();
      await fixture.whenStable();

      // mockEvent is on April 27 (weekDates[0]), 09:00-10:00
      const result = component.isOnDate(mockEvent, 0, 9);
      expect(result).toBe(true);
    });

    it('should return false when hour is outside event time', async () => {
      componentRef.setInput('id', 'ig-123');
      fixture.detectChanges();
      await fixture.whenStable();

      const result = component.isOnDate(mockEvent, 0, 11);
      expect(result).toBe(false);
    });

    it('should return false when event has undefined times', () => {
      const incomplete: EventItemDefinition = { id: 'x' };
      expect(component.isOnDate(incomplete, 0, 9)).toBe(false);
    });
  });

  describe('getDisplayableDays', () => {
    it('should return 7 days when workWeek is false', () => {
      expect(component.getDisplayableDays()).toEqual([0, 1, 2, 3, 4, 5, 6]);
    });

    it('should return 5 days when workWeek is true', () => {
      componentRef.setInput('workWeek', true);
      fixture.detectChanges();
      expect(component.getDisplayableDays()).toEqual([0, 1, 2, 3, 4]);
    });
  });

  describe('padWithLeadingZero', () => {
    it('should pad single digit with leading zero', () => {
      expect(component.padWithLeadingZero(5)).toBe('05');
    });

    it('should not pad double digit', () => {
      expect(component.padWithLeadingZero(12)).toBe('12');
    });
  });

  describe('getRepetition', () => {
    it('should return translated occurrence rate', () => {
      const result = component.getRepetition('EveryTimes');
      expect(Array.isArray(result)).toBe(true);
    });

    it('should handle undefined', () => {
      const result = component.getRepetition(undefined);
      expect(Array.isArray(result)).toBe(true);
    });
  });

  describe('getValueAsArray', () => {
    it('should return empty array for string value', () => {
      expect(component.getValueAsArray('10')).toEqual([]);
    });

    it('should return the array as-is for array value', () => {
      const arr = [{ event: mockEvent, show: true }];
      expect(component.getValueAsArray(arr)).toBe(arr);
    });
  });

  describe('viewRowElements', () => {
    it('should populate viewRowElements after loading events', async () => {
      componentRef.setInput('id', 'ig-123');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.viewRowElements().length).toBeGreaterThan(0);
    });

    it('should contain hour rows and day rows', async () => {
      componentRef.setInput('id', 'ig-123');
      fixture.detectChanges();
      await fixture.whenStable();

      const hourRows = component.viewRowElements().filter((r) => r.isHour);
      const dayRows = component.viewRowElements().filter((r) => !r.isHour);
      expect(hourRows.length).toBeGreaterThan(0);
      expect(dayRows.length).toBeGreaterThan(0);
    });
  });

  describe('coversMultipleMinuteRanges', () => {
    it('should return false for hour row (string value)', () => {
      const item = {
        isHour: true,
        isToday: false,
        isWeekendDay: false,
        multiplicity: new Map<string, number>(),
        value: '09',
        day: 0,
        dayDate: 27,
        dayName: 'Monday',
        showDetailsBox: false,
      };
      expect(component.coversMultipleMinuteRanges(item)).toBe(false);
    });
  });

  describe('showRightDetailsBox / showLeftDetailsBox', () => {
    it('should return false when isHour is true', () => {
      const item = {
        isHour: true,
        isToday: false,
        isWeekendDay: false,
        multiplicity: new Map<string, number>(),
        value: '09',
        day: 0,
        dayDate: 27,
        dayName: 'Monday',
        showDetailsBox: true,
      };
      expect(component.showRightDetailsBox(item)).toBe(false);
      expect(component.showLeftDetailsBox(item)).toBe(false);
    });

    it('should return true for showRightDetailsBox when day <= 1 and has events', () => {
      const item = {
        isHour: false,
        isToday: false,
        isWeekendDay: false,
        multiplicity: new Map<string, number>(),
        value: [{ event: mockEvent, show: true }],
        day: 0,
        dayDate: 27,
        dayName: 'Monday',
        showDetailsBox: true,
      };
      expect(component.showRightDetailsBox(item)).toBe(true);
      expect(component.showLeftDetailsBox(item)).toBe(false);
    });

    it('should return true for showLeftDetailsBox when day > 1 and has events', () => {
      const item = {
        isHour: false,
        isToday: false,
        isWeekendDay: false,
        multiplicity: new Map<string, number>(),
        value: [{ event: mockEvent, show: true }],
        day: 3,
        dayDate: 30,
        dayName: 'Thursday',
        showDetailsBox: true,
      };
      expect(component.showLeftDetailsBox(item)).toBe(true);
      expect(component.showRightDetailsBox(item)).toBe(false);
    });
  });

  describe('dayNames', () => {
    it('should call localizationService.getDayNames', () => {
      const names = component.dayNames;
      expect(mockLocalizationService.getDayNames).toHaveBeenCalledWith(
        'Monday',
        'full'
      );
      expect(names).toBe(mockDayNames);
    });
  });
});
