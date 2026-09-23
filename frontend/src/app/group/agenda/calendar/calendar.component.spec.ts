import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  EventItemDefinition,
  EventsService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { LocalizationService } from 'app/core/localization.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { TimeZoneHelperService } from 'app/core/timezone-helper.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CalendarComponent } from './calendar.component';

const mockEventsService = {
  getInterestGroupEventsAsync: vi.fn().mockResolvedValue([]),
};

const mockNodesService = {
  getNodeAsync: vi.fn().mockResolvedValue({ id: 'node1' } as ModelNode),
};

const mockPermEvalService = {
  isEveAdmin: vi.fn().mockReturnValue(false),
};

const mockUiMessageService = {
  addErrorMessage: vi.fn(),
};

const mockLocalizationService = {
  getDayNames: vi.fn().mockReturnValue({
    0: 'Sun',
    1: 'Mon',
    2: 'Tue',
    3: 'Wed',
    4: 'Thu',
    5: 'Fri',
    6: 'Sat',
  }),
};

const mockTimeZoneHelperService = {
  toLocalDateTime: vi.fn((events: EventItemDefinition[]) => events),
};

describe('CalendarComponent', () => {
  let component: CalendarComponent;
  let fixture: ComponentFixture<CalendarComponent>;
  let componentRef: ComponentRef<CalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarComponent],
      providers: [
        { provide: EventsService, useValue: mockEventsService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: LocalizationService, useValue: mockLocalizationService },
        { provide: TimeZoneHelperService, useValue: mockTimeZoneHelperService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CalendarComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('igId', 'ig1');
    componentRef.setInput('redisplay', false);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load eventRootNode when eventRootId is provided', async () => {
    componentRef.setInput('eventRootId', 'root1');
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({ id: 'root1' });
    expect(component.eventRootNode()).toEqual({ id: 'node1' });
  });

  it('should not load eventRootNode when eventRootId is not provided', async () => {
    mockNodesService.getNodeAsync.mockClear();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockNodesService.getNodeAsync).not.toHaveBeenCalled();
  });

  it('should initialize calendar days on initDays', async () => {
    await component.initDays();
    expect(mockEventsService.getInterestGroupEventsAsync).toHaveBeenCalled();
    expect(component.calendarDays()).toBeDefined();
    expect(component.calendarDays().length).toBeGreaterThan(0);
  });

  it('should return day names from localization service', () => {
    expect(component.dayNames).toEqual({
      0: 'Sun',
      1: 'Mon',
      2: 'Tue',
      3: 'Wed',
      4: 'Thu',
      5: 'Fri',
      6: 'Sat',
    });
    expect(mockLocalizationService.getDayNames).toHaveBeenCalledWith(
      'Sunday',
      'short'
    );
  });

  it('should detect weekend days', async () => {
    const saturday = {
      type: 'currentMonth',
      date: new Date(2026, 3, 25),
      events: [],
      showDetailsBox: false,
    };
    const sunday = {
      type: 'currentMonth',
      date: new Date(2026, 3, 26),
      events: [],
      showDetailsBox: false,
    };
    const monday = {
      type: 'currentMonth',
      date: new Date(2026, 3, 27),
      events: [],
      showDetailsBox: false,
    };

    expect(component.isWeekendDay(saturday)).toBe(true);
    expect(component.isWeekendDay(sunday)).toBe(true);
    expect(component.isWeekendDay(monday)).toBe(false);
  });

  it('should check if date is after today', async () => {
    await component.initDays();
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 1);
    expect(component.checkIfDateAfterToday(futureDate)).toBe(true);

    const pastDate = new Date(2020, 0, 1);
    expect(component.checkIfDateAfterToday(pastDate)).toBe(false);
  });

  it('should emit popupCreateEventEmitter when onPopupCreateEventWithCalendarDate is called', () => {
    const emitSpy = vi.spyOn(component.popupCreateEventEmitter, 'emit');
    const date = new Date(2026, 3, 15);
    component.onPopupCreateEventWithCalendarDate(date);
    expect(emitSpy).toHaveBeenCalledWith(date);
  });

  it('should emit popupDeleteEventEmitter when deleteEvent is called', () => {
    const emitSpy = vi.spyOn(component.popupDeleteEventEmitter, 'emit');
    const event: EventItemDefinition = { id: 'evt1', title: 'Test Event' };
    component.deleteEvent(event);
    expect(emitSpy).toHaveBeenCalledWith(event);
  });

  it('should delegate isEveAdmin to permEvalService', () => {
    component.eventRootNode.set({ id: 'root1' } as ModelNode);
    mockPermEvalService.isEveAdmin.mockReturnValue(true);
    expect(component.isEveAdmin()).toBe(true);
    expect(mockPermEvalService.isEveAdmin).toHaveBeenCalledWith(
      component.eventRootNode()
    );
  });

  it('should enable details box for a matching date with events', async () => {
    const date = new Date(2026, 3, 15);
    const event: EventItemDefinition = { id: 'evt1', title: 'Test' };
    component.calendarDays.set([
      { type: 'currentMonth', date, events: [event], showDetailsBox: false },
    ]);
    component.enableDetailsBox(date);
    expect(component.calendarDays()[0].showDetailsBox).toBe(true);
  });

  it('should not enable details box when no events', () => {
    const date = new Date(2026, 3, 15);
    component.calendarDays.set([
      { type: 'currentMonth', date, events: [], showDetailsBox: false },
    ]);
    component.enableDetailsBox(date);
    expect(component.calendarDays()[0].showDetailsBox).toBe(false);
  });

  it('should disable details box for a matching date', () => {
    const date = new Date(2026, 3, 15);
    component.calendarDays.set([
      { type: 'currentMonth', date, events: [], showDetailsBox: true },
    ]);
    component.disableDetailsBox(date);
    expect(component.calendarDays()[0].showDetailsBox).toBe(false);
  });

  it('should return repetition translation', () => {
    const result = component.getRepetition('EveryTimes');
    expect(result).toBeDefined();
    expect(Array.isArray(result)).toBe(true);
  });

  it('should set processing to false after initDays completes', async () => {
    await component.initDays();
    expect(component.processing).toBe(false);
  });

  it('should emit processingEventEmitter with "month" after loading', async () => {
    const emitSpy = vi.spyOn(component.processingEventEmitter, 'emit');
    await component.initDays();
    expect(emitSpy).toHaveBeenCalledWith('month');
  });

  it('should call toLocalDateTime on fetched events', async () => {
    const events: EventItemDefinition[] = [{ id: 'e1', title: 'Evt' }];
    mockEventsService.getInterestGroupEventsAsync.mockResolvedValue(events);
    await component.initDays();
    expect(mockTimeZoneHelperService.toLocalDateTime).toHaveBeenCalledWith(
      events
    );
  });
});
