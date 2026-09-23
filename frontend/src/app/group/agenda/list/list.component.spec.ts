import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  EventsService,
  InterestGroupService,
  NodesService,
  PagedEventItemDefinition,
} from 'app/core/generated/circabc';
import { SaveAsService } from 'app/core/save-as.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { ListComponent } from './list.component';

describe('ListComponent', () => {
  let component: ListComponent;
  let fixture: ComponentFixture<ListComponent>;
  let paramsSubject: Subject<{ [key: string]: string }>;
  let mockEventsService: {
    getInterestGroupListEventsAsync: ReturnType<typeof vi.fn>;
  };
  let mockInterestGroupService: {
    getInterestGroupAsync: ReturnType<typeof vi.fn>;
  };
  let mockNodesService: { getNodeAsync: ReturnType<typeof vi.fn> };
  let mockPermEvalService: { isEveAdmin: ReturnType<typeof vi.fn> };
  let mockSaveAsService: { saveUrlAs: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    paramsSubject = new Subject();

    mockEventsService = {
      getInterestGroupListEventsAsync: vi.fn().mockResolvedValue({
        data: [{ id: '1', title: 'Test Event' }],
        total: 1,
      } as PagedEventItemDefinition),
    };
    mockInterestGroupService = {
      getInterestGroupAsync: vi.fn().mockResolvedValue({
        name: 'Test IG',
        permissions: {},
        eventId: 'event-root-id',
      }),
    };
    mockNodesService = {
      getNodeAsync: vi
        .fn()
        .mockResolvedValue({ id: 'event-root-id', permissions: {} }),
    };
    mockPermEvalService = { isEveAdmin: vi.fn().mockReturnValue(false) };
    mockSaveAsService = { saveUrlAs: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ListComponent],
      providers: [
        provideRouter([]),
        provideNativeDateAdapter(),
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: EventsService, useValue: mockEventsService },
        { provide: InterestGroupService, useValue: mockInterestGroupService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: BASE_PATH, useValue: 'http://localhost' },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load events when route params emit', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    expect(component.igId()).toBe('ig-123');
    expect(
      mockEventsService.getInterestGroupListEventsAsync
    ).toHaveBeenCalledWith({
      id: 'ig-123',
      filter: 'Exact',
      exactDate: expect.any(String),
      limit: 10,
      page: 1,
      sort: 'appointmentDate_DESC',
    });
    expect(component.appointments()).toEqual([
      { id: '1', title: 'Test Event' },
    ]);
    expect(component.totalItems()).toBe(1);
  });

  it('should fetch event root node on first load', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    expect(mockInterestGroupService.getInterestGroupAsync).toHaveBeenCalledWith(
      {
        id: 'ig-123',
      }
    );
    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
      id: 'event-root-id',
    });
    expect(component.eventRootNode()?.id).toBe('event-root-id');
  });

  it('should not re-fetch event root node on subsequent loads', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    expect(component.eventRootNode()).toBeDefined();
    const callCount = mockNodesService.getNodeAsync.mock.calls.length;

    await component.goToPage(2);
    await fixture.whenStable();

    expect(mockNodesService.getNodeAsync.mock.calls).toHaveLength(callCount);
  });

  it('should delegate isEveAdmin to PermissionEvaluatorService', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    const result = component.isEveAdmin();
    expect(mockPermEvalService.isEveAdmin).toHaveBeenCalledWith(
      component.eventRootNode()
    );
    expect(result).toBe(false);
  });

  it('should change filter and reset page to 1', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();
    mockEventsService.getInterestGroupListEventsAsync.mockClear();

    component.changeFilter('Previous');
    await fixture.whenStable();

    expect(component.filter).toBe('Previous');
    expect(component.listingOptions().page).toBe(1);
    expect(
      mockEventsService.getInterestGroupListEventsAsync
    ).toHaveBeenCalledWith({
      id: 'ig-123',
      filter: 'Previous',
      exactDate: expect.any(String),
      limit: 10,
      page: 1,
      sort: 'appointmentDate_ASC',
    });
  });

  it('should update page on goToPage', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();
    mockEventsService.getInterestGroupListEventsAsync.mockClear();

    component.goToPage(3);
    await fixture.whenStable();

    expect(component.listingOptions().page).toBe(3);
    expect(
      mockEventsService.getInterestGroupListEventsAsync
    ).toHaveBeenCalled();
  });

  it('should change limit and reset page to 1', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    component.changeLimit(25);
    await fixture.whenStable();

    expect(component.listingOptions().limit).toBe(25);
    expect(component.listingOptions().page).toBe(1);
  });

  it('should set deleteEventShowModal on popupDeleteEvent', () => {
    const event = { id: '1', title: 'To Delete' };
    component.popupDeleteEvent(event);

    expect(component.deleteEventShowModal).toBe(true);
    expect(component.eventToDelete).toBe(event);
  });

  it('should call saveAsService.saveUrlAs on export', async () => {
    fixture.detectChanges();
    paramsSubject.next({ id: 'ig-123' });
    await fixture.whenStable();

    const result = component.export();

    expect(mockSaveAsService.saveUrlAs).toHaveBeenCalledWith(
      expect.stringContaining('/groups/ig-123/events/export'),
      'Events.csv'
    );
    expect(result).toBe(false);
  });
});
