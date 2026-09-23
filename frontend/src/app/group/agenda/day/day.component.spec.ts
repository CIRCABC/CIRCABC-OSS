import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { AgendaHelperService } from 'app/core/agenda-helper.service';
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
import { DayComponent } from './day.component';

describe('DayComponent', () => {
  let component: DayComponent;
  let componentRef: ComponentRef<DayComponent>;
  let fixture: ComponentFixture<DayComponent>;

  const mockEvents: EventItemDefinition[] = [
    {
      id: 'event1',
      appointmentType: 'Event',
      title: 'Test Event',
      appointmentDate: '2026-04-30',
      startTime: '09:00',
      endTime: '10:00',
      timeZone: 'Europe/Brussels',
    },
    {
      id: 'event2',
      appointmentType: 'Meeting',
      title: 'Test Meeting',
      appointmentDate: '2026-04-30',
      startTime: '14:00',
      endTime: '15:30',
      timeZone: 'Europe/Brussels',
    },
  ];

  const mockUserService = {
    getUserEventsPeriodAsync: vi.fn().mockResolvedValue(mockEvents),
  };

  const mockEventsService = {
    getInterestGroupEventsAsync: vi.fn().mockResolvedValue(mockEvents),
  };

  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('testuser'),
  };

  const mockAgendaHelperService = {
    isShowRibbons: vi.fn().mockReturnValue(false),
    getEventDisplayColor: vi.fn().mockReturnValue('#ff0000'),
    setEventDisplayColor: vi.fn(),
    toggleShowRibbons: vi.fn().mockReturnValue(true),
  };

  const mockLocalizationService = {
    getDayNames: vi.fn().mockReturnValue({ Mon: 'Monday', Tue: 'Tuesday' }),
  };

  const mockTimeZoneHelperService = {
    toLocalDateTime: vi
      .fn()
      .mockImplementation((events: EventItemDefinition[]) => events),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DayComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: UserService, useValue: mockUserService },
        { provide: EventsService, useValue: mockEventsService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: AgendaHelperService, useValue: mockAgendaHelperService },
        { provide: LocalizationService, useValue: mockLocalizationService },
        { provide: TimeZoneHelperService, useValue: mockTimeZoneHelperService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DayComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('date', new Date(2026, 3, 30));
    componentRef.setInput('displayFromHour', 8);
    componentRef.setInput('displayToHour', 18);
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnChanges', () => {
    it('should load events for interest group when id is set', async () => {
      componentRef.setInput('id', 'group123');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(
        mockEventsService.getInterestGroupEventsAsync
      ).toHaveBeenCalledWith({
        id: 'group123',
        startDate: expect.any(String),
        endDate: expect.any(String),
      });
      expect(component.events).toEqual(mockEvents);
    });

    it('should load user events when meMode is true', async () => {
      componentRef.setInput('meMode', true);
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockUserService.getUserEventsPeriodAsync).toHaveBeenCalledWith({
        userId: 'testuser',
        exactDate: expect.any(String),
        period: 'Exact',
      });
      expect(component.events).toEqual(mockEvents);
    });

    it('should set showRibbons from AgendaHelperService', async () => {
      mockAgendaHelperService.isShowRibbons.mockReturnValue(true);
      componentRef.setInput('id', 'group123');
      component.ngOnChanges();
      await new Promise((resolve) => setTimeout(resolve));

      expect(component.showRibbons()).toBe(true);
    });
  });

  describe('isEventAtThisTime', () => {
    it('should return true when event starts within the time slot', () => {
      expect(component.isEventAtThisTime(540, 540, 600)).toBe(true);
    });

    it('should return true when event spans across the time slot', () => {
      expect(component.isEventAtThisTime(550, 540, 600)).toBe(true);
    });

    it('should return true when event ends within the time slot', () => {
      expect(component.isEventAtThisTime(585, 540, 600)).toBe(true);
    });

    it('should return false when event is outside the time slot', () => {
      expect(component.isEventAtThisTime(700, 540, 600)).toBe(false);
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

  describe('isEvent / isMeeting', () => {
    it('should return true for Event type', () => {
      expect(component.isEvent({ appointmentType: 'Event' })).toBe(true);
    });

    it('should return false for Meeting type in isEvent', () => {
      expect(component.isEvent({ appointmentType: 'Meeting' })).toBe(false);
    });

    it('should return true for Meeting type', () => {
      expect(component.isMeeting({ appointmentType: 'Meeting' })).toBe(true);
    });
  });

  describe('getStyle', () => {
    it('should return arrow style when event starts at current time', () => {
      const item = {
        event: mockEvents[0],
        assignedColor: '#ff0000',
        startTime: 540,
        currentTime: 540,
        endTime: 600,
        show: true,
      };
      const style = component.getStyle(item);
      expect(style).toEqual({
        width: 0,
        height: 0,
        'border-top': '4px solid transparent',
        'border-bottom': '4px solid transparent',
        'border-left': '4px solid #ff0000',
      });
    });

    it('should return background style when event is in progress', () => {
      const item = {
        event: mockEvents[0],
        assignedColor: '#ff0000',
        startTime: 540,
        currentTime: 555,
        endTime: 600,
        show: true,
      };
      const style = component.getStyle(item) as Record<string, string>;
      expect(style['background-color']).toBe('#ff0000');
      expect(style['width']).toBe('100%');
    });
  });

  describe('toggleRibbons', () => {
    it('should toggle ribbons via AgendaHelperService', () => {
      component.toggleRibbons();
      expect(mockAgendaHelperService.toggleShowRibbons).toHaveBeenCalled();
      expect(component.showRibbons()).toBe(true);
    });
  });

  describe('dayNames', () => {
    it('should delegate to localizationService', () => {
      const result = component.dayNames;
      expect(mockLocalizationService.getDayNames).toHaveBeenCalledWith(
        'Monday',
        'full',
        5
      );
      expect(result).toEqual({ Mon: 'Monday', Tue: 'Tuesday' });
    });
  });

  describe('getValueAsArray', () => {
    it('should return empty array for string value', () => {
      expect(component.getValueAsArray('08:')).toEqual([]);
    });

    it('should return the array as-is for ShowableEventItemDefinition[]', () => {
      const arr = [
        {
          event: mockEvents[0],
          assignedColor: '#ff0000',
          startTime: 540,
          currentTime: 540,
          endTime: 600,
          show: true,
        },
      ];
      expect(component.getValueAsArray(arr)).toBe(arr);
    });
  });

  describe('viewRowElements generation', () => {
    it('should generate view rows after loading events', async () => {
      componentRef.setInput('id', 'group123');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.viewRowElements().length).toBeGreaterThan(0);
      // Each hour has 1 hour row + 4 minute rows (0, 15, 30, 45)
      // From hour 8 to 18 inclusive = 11 hours * 5 rows = 55
      expect(component.viewRowElements()).toHaveLength(55);
    });
  });
});
