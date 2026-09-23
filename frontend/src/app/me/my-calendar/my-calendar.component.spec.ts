import { I18nSelectPipe } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideNativeDateAdapter } from '@angular/material/core';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { AgendaHelperService } from 'app/core/agenda-helper.service';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  EventItemDefinition,
  EventsService,
  NodesService,
  UserService,
} from 'app/core/generated/circabc';
import { LocalizationService } from 'app/core/localization.service';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { TimeZoneHelperService } from 'app/core/timezone-helper.service';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { MyCalendarComponent } from './my-calendar.component';

describe('MyCalendarComponent', () => {
  let component: MyCalendarComponent;

  const mockUserService = {
    getUserEventsPeriodAsync: vi.fn().mockResolvedValue([]),
  };

  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('testuser'),
  };

  const mockAgendaHelperService = {
    getMyCalendarViewState: vi.fn().mockReturnValue('month'),
    saveMyCalendarViewState: vi.fn(),
  };

  const mockLocalizationService = {
    getMonthsNames: vi.fn().mockReturnValue({}),
    getDayNames: vi.fn().mockReturnValue({}),
  };

  const mockTimeZoneHelperService = {
    toLocalDateTime: vi
      .fn()
      .mockImplementation((events: EventItemDefinition[]) => events),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  const mockPermissionEvaluatorService = {
    isEveAdmin: vi.fn().mockReturnValue(false),
  };

  const mockEventsService = {
    getInterestGroupEvents: vi.fn().mockReturnValue(of([])),
  };

  const mockNodesService = {
    getNode: vi.fn().mockReturnValue(of({})),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyCalendarComponent],
      providers: [
        provideNativeDateAdapter(),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: AgendaHelperService, useValue: mockAgendaHelperService },
        { provide: LocalizationService, useValue: mockLocalizationService },
        { provide: TimeZoneHelperService, useValue: mockTimeZoneHelperService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        {
          provide: PermissionEvaluatorService,
          useValue: mockPermissionEvaluatorService,
        },
        { provide: EventsService, useValue: mockEventsService },
        { provide: NodesService, useValue: mockNodesService },
      ],
    })
      .overrideComponent(MyCalendarComponent, {
        set: {
          imports: [
            ReactiveFormsModule,
            TranslocoModule,
            I18nSelectPipe,
            SetTitlePipe,
          ],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    const fixture = TestBed.createComponent(MyCalendarComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize with default values on ngOnInit', async () => {
    await component.ngOnInit();

    expect(component.workWeek).toBe(true);
    expect(component.view).toBe('month');
    expect(component.from).toBe(8);
    expect(component.to).toBe(21);
    expect(component.changeDateForm).toBeDefined();
    expect(mockAgendaHelperService.getMyCalendarViewState).toHaveBeenCalled();
  });

  it('should call getUserEventsPeriod in getEvents', async () => {
    await component.ngOnInit();
    const startDate = new Date(2026, 3, 1);

    await component.getEvents(startDate, new Date());

    expect(mockUserService.getUserEventsPeriodAsync).toHaveBeenCalledWith({
      userId: 'testuser',
      exactDate: expect.any(String),
      period: 'Future',
    });
  });

  it('should update view and save state on selectView', () => {
    component.selectView('week');

    expect(component.view).toBe('week');
    expect(
      mockAgendaHelperService.saveMyCalendarViewState
    ).toHaveBeenCalledWith('week');
  });

  it('should reinitialize day hour range when selecting day view', () => {
    component.from = 0;
    component.to = 0;

    component.selectView('day');

    expect(component.from).toBe(8);
    expect(component.to).toBe(21);
  });

  it('should set from value from string', () => {
    component.selectFrom('10');
    expect(component.from).toBe(10);
  });

  it('should set to value from string', () => {
    component.selectTo('18');
    expect(component.to).toBe(18);
  });

  it('should toggle workWeek', () => {
    component.workWeek = true;
    component.toggleWorkWeek();
    expect(component.workWeek).toBe(false);

    component.toggleWorkWeek();
    expect(component.workWeek).toBe(true);
  });

  it('should return a formatted date string', async () => {
    await component.ngOnInit();
    const result = component.getFormattedDate();
    expect(result).toBeDefined();
    expect(typeof result).toBe('string');
  });

  it('should delegate monthNames to localizationService', () => {
    const result = component.monthNames;
    expect(mockLocalizationService.getMonthsNames).toHaveBeenCalled();
    expect(result).toEqual({});
  });

  it('should delegate completeDayNames to localizationService', () => {
    const result = component.completeDayNames;
    expect(mockLocalizationService.getDayNames).toHaveBeenCalled();
    expect(result).toEqual({});
  });

  describe('navigation', () => {
    beforeEach(async () => {
      await component.ngOnInit();
      mockUserService.getUserEventsPeriodAsync.mockClear();
    });

    it('should advance one month on nextMonth', async () => {
      const initialMonth = component.date().getMonth();
      await component.nextMonth();
      expect(component.date().getMonth()).toBe((initialMonth + 1) % 12);
    });

    it('should go back one month on previousMonth', async () => {
      const initialMonth = component.date().getMonth();
      await component.previousMonth();
      expect(component.date().getMonth()).toBe((initialMonth + 11) % 12);
    });

    it('should advance one day on nextDay', async () => {
      const initial = component.date().getTime();
      await component.nextDay();
      expect(component.date().getTime() - initial).toBe(24 * 60 * 60 * 1000);
    });

    it('should go back one day on previousDay', async () => {
      const initial = component.date().getTime();
      await component.previousDay();
      expect(initial - component.date().getTime()).toBe(24 * 60 * 60 * 1000);
    });

    it('should advance 7 days on nextWeek', async () => {
      const initial = component.date().getTime();
      await component.nextWeek();
      expect(component.date().getTime() - initial).toBe(
        7 * 24 * 60 * 60 * 1000
      );
    });

    it('should go back 7 days on previousWeek', async () => {
      const initial = component.date().getTime();
      await component.previousWeek();
      expect(initial - component.date().getTime()).toBe(
        7 * 24 * 60 * 60 * 1000
      );
    });
  });
});
