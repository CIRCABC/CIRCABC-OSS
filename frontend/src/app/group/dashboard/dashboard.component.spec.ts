import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { DashboardService, InterestGroup } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { BehaviorSubject, of } from 'rxjs';
import { vi } from 'vitest';
import { DashboardComponent } from './dashboard.component';

function makeGroup(
  overrides: Partial<InterestGroup['permissions']> = {}
): InterestGroup {
  return {
    name: 'Test Group',
    id: 'group-123',
    permissions: {
      information: 'InfAccess',
      library: 'LibAccess',
      directory: 'DirAccess',
      newsgroup: 'NwsAccess',
      event: 'EveAccess',
      ...overrides,
    },
  } as InterestGroup;
}

describe('DashboardComponent', () => {
  const dataSubject = new BehaviorSubject<{ group: InterestGroup }>({
    group: makeGroup(),
  });

  const mockRoute = {
    parent: { data: dataSubject.asObservable() },
  };

  const mockDashboardService = {
    getGroupDashboardAsync: vi
      .fn()
      .mockResolvedValue({ groupId: 'group-123', entries: [] }),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  let component: DashboardComponent;

  beforeEach(async () => {
    vi.clearAllMocks();
    mockDashboardService.getGroupDashboardAsync.mockResolvedValue({
      groupId: 'group-123',
      entries: [],
    });

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: DashboardService, useValue: mockDashboardService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(DashboardComponent, {
        set: {
          imports: [TranslocoModule],
          schemas: [NO_ERRORS_SCHEMA],
          template: '',
        },
      })
      .compileComponents();

    const fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load group and set display flags based on permissions', () => {
    expect(component.group()).toBeDefined();
    expect(component.igId()).toBe('group-123');
    expect(component.displayWhatsnewBox()).toBe(true);
    expect(component.displayMembersBox()).toBe(true);
    expect(component.displayForumsBox()).toBe(true);
    expect(component.displayEventsBox()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('should set display flags to false when permissions deny access', async () => {
    dataSubject.next({
      group: makeGroup({
        information: 'InfNoAccess',
        library: 'LibNoAccess',
        directory: 'DirNoAccess',
        newsgroup: 'NwsNoAccess',
        event: 'EveNoAccess',
      }),
    });

    await new Promise((r) => setTimeout(r, 0));

    expect(component.displayWhatsnewBox()).toBe(false);
    expect(component.displayMembersBox()).toBe(false);
    expect(component.displayForumsBox()).toBe(false);
    expect(component.displayEventsBox()).toBe(false);
  });

  it('should call getGroupDashboard when whatsnew box is displayed', async () => {
    // Emit a fresh value to trigger loadGroup -> loadDashboard
    mockDashboardService.getGroupDashboardAsync.mockClear();
    dataSubject.next({ group: makeGroup() });
    await new Promise((r) => setTimeout(r, 0));
    expect(mockDashboardService.getGroupDashboardAsync).toHaveBeenCalledWith({
      id: 'group-123',
    });
    expect(component.timeline()).toEqual({ groupId: 'group-123', entries: [] });
  });

  it('should not call getGroupDashboard when whatsnew box is hidden', async () => {
    mockDashboardService.getGroupDashboardAsync.mockClear();

    dataSubject.next({
      group: makeGroup({
        information: 'InfNoAccess',
        library: 'LibNoAccess',
      }),
    });

    await new Promise((r) => setTimeout(r, 0));

    expect(mockDashboardService.getGroupDashboardAsync).not.toHaveBeenCalled();
  });

  it('should handle dashboard load error and show error message', async () => {
    mockDashboardService.getGroupDashboardAsync.mockRejectedValue(
      new Error('fail')
    );

    dataSubject.next({ group: makeGroup() });

    await new Promise((r) => setTimeout(r, 0));

    expect(component.timeline()).toEqual({});
    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
  });
});
