import { I18nSelectPipe } from '@angular/common';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { AgendaHelperService } from 'app/core/agenda-helper.service';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  EventItemDefinition,
  InterestGroupService,
  NodesService,
} from 'app/core/generated/circabc';
import { LocalizationService } from 'app/core/localization.service';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { AgendaComponent } from './agenda.component';

describe('AgendaComponent', () => {
  let component: AgendaComponent;

  const paramsSubject = new Subject<{ [key: string]: string }>();

  const mockInterestGroupService = {
    getInterestGroupAsync: vi.fn().mockResolvedValue({
      name: 'Test IG',
      permissions: {},
      eventId: 'event-123',
    }),
  };

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue({ id: 'event-123' }),
  };

  const mockPermEvalService = {
    isEveAdmin: vi.fn().mockReturnValue(false),
  };

  const mockLocalizationService = {
    getMonthsNames: vi.fn().mockReturnValue({}),
    getDayNames: vi.fn().mockReturnValue({}),
  };

  const mockAgendaHelperService = {
    getAgendaViewState: vi.fn().mockReturnValue('month'),
    saveAgendaViewState: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AgendaComponent],
      providers: [
        provideNativeDateAdapter(),
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: LocalizationService, useValue: mockLocalizationService },
        { provide: AgendaHelperService, useValue: mockAgendaHelperService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(AgendaComponent, {
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

    const fixture = TestBed.createComponent(AgendaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize with default values on ngOnInit', () => {
    expect(component.view).toBe('month');
    expect(component.workWeek).toBe(true);
    expect(component.from).toBe(8);
    expect(component.to).toBe(21);
    expect(component.currentDate).toBeDefined();
  });

  it('should load interest group when route params emit', async () => {
    paramsSubject.next({ id: 'ig-1' });
    await vi.waitFor(() => {
      expect(component.igLoaded()).toBe(true);
    });
    expect(component.igId()).toBe('ig-1');
    expect(mockInterestGroupService.getInterestGroupAsync).toHaveBeenCalledWith(
      {
        id: 'ig-1',
      }
    );
    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
      id: 'event-123',
    });
  });

  it('should delegate isEveAdmin to permEvalService', () => {
    component.eventRootNode.set({ id: 'event-123' });
    mockPermEvalService.isEveAdmin.mockReturnValue(true);
    expect(component.isEveAdmin()).toBe(true);
    expect(mockPermEvalService.isEveAdmin).toHaveBeenCalledWith({
      id: 'event-123',
    });
  });

  it('should open create event modal with calendar day', () => {
    const date = new Date(2026, 3, 15);
    component.popupCreateEventWithCalendarDay(date);
    expect(component.currentDate).toBe(date);
    expect(component.createEventShowModal).toBe(true);
  });

  it('should open delete event modal', () => {
    const event: EventItemDefinition = { id: 'evt-1' };
    component.popupDeleteEvent(event);
    expect(component.eventToDelete).toBe(event);
    expect(component.deleteEventShowModal).toBe(true);
  });

  it('should not open delete event modal for undefined event', () => {
    component.deleteEventShowModal = false;
    component.popupDeleteEvent(undefined as unknown as EventItemDefinition);
    expect(component.deleteEventShowModal).toBe(false);
  });

  it('should set createEventShowModal with today for createEventWithToday', () => {
    component.createEventWithToday(false);
    expect(component.createEventShowModal).toBe(true);
    expect(component.isEvent).toBe(false);
  });

  it('should toggle redisplayCalendar on redisplayCalendarAfterSave', () => {
    const initial = component.redisplayCalendar;
    component.redisplayCalendarAfterSave();
    expect(component.redisplayCalendar).toBe(!initial);
  });

  it('should selectView and save state', () => {
    component.selectView('week');
    expect(component.view).toBe('week');
    expect(mockAgendaHelperService.saveAgendaViewState).toHaveBeenCalledWith(
      'week'
    );
  });

  it('should reset hour range when selecting day view', () => {
    component.from = 0;
    component.to = 0;
    component.selectView('day');
    expect(component.from).toBe(8);
    expect(component.to).toBe(21);
  });

  it('should selectFrom and selectTo', () => {
    component.selectFrom('10');
    component.selectTo('18');
    expect(component.from).toBe(10);
    expect(component.to).toBe(18);
  });

  it('should toggle workWeek', () => {
    expect(component.workWeek).toBe(true);
    component.toggleWorkWeek();
    expect(component.workWeek).toBe(false);
  });

  it('should navigate to next and previous month', () => {
    component.currentDate = new Date(2026, 0, 15);
    component.nextMonth();
    expect(component.currentDate.getMonth()).toBe(1);
    expect(component.processing).toBe(true);

    component.previousMonth();
    expect(component.currentDate.getMonth()).toBe(0);
  });

  it('should navigate to next and previous day', () => {
    component.currentDate = new Date(2026, 0, 10);
    component.nextDay();
    expect(component.currentDate.getDate()).toBe(11);

    component.previousDay();
    expect(component.currentDate.getDate()).toBe(10);
  });

  it('should navigate to next and previous week', () => {
    component.currentDate = new Date(2026, 0, 10);
    component.nextWeek();
    expect(component.currentDate.getDate()).toBe(17);

    component.previousWeek();
    expect(component.currentDate.getDate()).toBe(10);
  });

  it('should finishProcessing set view and clear processing', () => {
    component.processing = true;
    component.finishProcessing('day');
    expect(component.view).toBe('day');
    expect(component.processing).toBe(false);
    expect(mockAgendaHelperService.saveAgendaViewState).toHaveBeenCalledWith(
      'day'
    );
  });
});
