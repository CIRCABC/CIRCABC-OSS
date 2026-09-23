import { Location } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  Node as ModelNode,
  NodesService,
  NotificationConfiguration,
  NotificationService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';

import { NotificationStatusComponent } from './notification-status.component';

const mockNode: ModelNode = {
  id: 'node1',
  name: 'Test Node',
  properties: { circaIGRootMasterGroup: 'group1' },
};

const mockPagedConfigurations = {
  data: [
    { userName: 'user1', status: 'SUBSCRIBED' as const, authority: 'auth1' },
  ],
  total: 1,
};

const mockPagedUsers = {
  data: [{ userName: 'user1', firstName: 'John', lastName: 'Doe' }],
  total: 1,
};

describe('NotificationStatusComponent', () => {
  let component: NotificationStatusComponent;
  let paramsSubject: Subject<Record<string, string>>;
  let queryParamsSubject: Subject<Record<string, string>>;

  const mockNotificationService = {
    getNotificationConfigurationsAsync: vi
      .fn()
      .mockResolvedValue(mockPagedConfigurations),
    getNotificationSubscribedUsersAsync: vi
      .fn()
      .mockResolvedValue(mockPagedUsers),
    postNotificationStatusAsync: vi.fn().mockResolvedValue(undefined),
    deleteNotificationAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue(mockNode),
  };

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue({ userId: 'testUser', uiLang: 'en' }),
  };

  const mockLocation = {
    back: vi.fn(),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();
    queryParamsSubject = new Subject();

    vi.clearAllMocks();
    mockNotificationService.getNotificationConfigurationsAsync.mockResolvedValue(
      mockPagedConfigurations
    );
    mockNotificationService.getNotificationSubscribedUsersAsync.mockResolvedValue(
      mockPagedUsers
    );
    mockNodesService.getNodeAsync.mockResolvedValue(mockNode);
    mockLoginService.getUser.mockReturnValue({
      userId: 'testUser',
      uiLang: 'en',
    });

    await TestBed.configureTestingModule({
      imports: [NotificationStatusComponent],
      providers: [
        { provide: NotificationService, useValue: mockNotificationService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: Location, useValue: mockLocation },
        {
          provide: ActivatedRoute,
          useValue: {
            params: paramsSubject.asObservable(),
            queryParams: queryParamsSubject.asObservable(),
          },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(NotificationStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize with empty arrays', () => {
    expect(component.notificationConfigurations()).toEqual([]);
    expect(component.subscribedUsers()).toEqual([]);
  });

  it('should set fromPage from query params', () => {
    queryParamsSubject.next({ from: 'information' });
    expect(component.fromPage()).toBe('information');
  });

  it('should load notification status when route params have nodeId', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({ id: 'node1' });
    expect(component.ready()).toBe(true);
    expect(component.notificationConfigurations()).toEqual(
      mockPagedConfigurations.data
    );
    expect(component.subscribedUsers()).toEqual(mockPagedUsers.data);
  });

  it('should not load when nodeId is undefined', async () => {
    paramsSubject.next({ id: 'ig1' });
    await new Promise((resolve) => setTimeout(resolve));

    expect(mockNodesService.getNodeAsync).not.toHaveBeenCalled();
    expect(component.ready()).toBe(false);
  });

  it('should set fromIG to false when circaIGRootMasterGroup is missing', async () => {
    mockNodesService.getNodeAsync.mockResolvedValue({
      id: 'node2',
      properties: {},
    });
    paramsSubject.next({ id: 'ig1', nodeId: 'node2' });
    await new Promise((resolve) => setTimeout(resolve));

    expect(component.fromIG).toBe(false);
  });

  it('should toggle configuration status from SUBSCRIBED to UNSUBSCRIBED', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    const config: NotificationConfiguration = {
      status: 'SUBSCRIBED',
      authority: 'auth1',
    };
    await component.toggleConfigurationStatus(config);

    expect(
      mockNotificationService.postNotificationStatusAsync
    ).toHaveBeenCalledWith({
      id: 'node1',
      authority: 'auth1',
      status: 'UNSUBSCRIBED',
    });
  });

  it('should toggle configuration status from UNSUBSCRIBED to SUBSCRIBED', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    const config: NotificationConfiguration = {
      status: 'UNSUBSCRIBED',
      authority: 'auth1',
    };
    await component.toggleConfigurationStatus(config);

    expect(
      mockNotificationService.postNotificationStatusAsync
    ).toHaveBeenCalledWith({
      id: 'node1',
      authority: 'auth1',
      status: 'SUBSCRIBED',
    });
  });

  it('should delete configuration', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    await component.deleteConfiguration({ authority: 'auth1' });

    expect(
      mockNotificationService.deleteNotificationAsync
    ).toHaveBeenCalledWith({
      id: 'node1',
      authority: 'auth1',
    });
  });

  it('should go to page for configurations', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    await component.goToPageConfigurations(2);
    expect(component.listingOptionsConfigurations().page).toBe(2);
  });

  it('should go to page for users', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    await component.goToPageUsers(3);
    expect(component.listingOptionsUsers().page).toBe(3);
  });

  it('should call location.back on goBack', () => {
    component.goBack();
    expect(mockLocation.back).toHaveBeenCalled();
  });

  it('should identify forum newsgroups name', () => {
    expect(component.isForumNewsgroupsName('Newsgroups', 'forums')).toBe(true);
    expect(component.isForumNewsgroupsName('Other', 'forums')).toBe(false);
    expect(component.isForumNewsgroupsName('Newsgroups', 'library')).toBe(
      false
    );
  });

  it('should reset search config and reload', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    component.searchFormConfig.patchValue({
      configType: 'test',
      configUserName: 'u',
      configStatus: 'SUBSCRIBED',
    });
    await component.resetSearchConfig();

    expect(component.searchFormConfig.value).toEqual({
      configType: '',
      configUserName: '',
      configStatus: '',
    });
  });

  it('should reset search form and reload users', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    component.searchForm.patchValue({
      userName: 'u',
      firstName: 'f',
      lastName: 'l',
      email: 'e',
    });
    await component.resetSearch();

    expect(component.searchForm.value).toEqual({
      userName: '',
      firstName: '',
      lastName: '',
      email: '',
    });
  });

  it('should refresh and close add modal', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    component.showAddModal.set(true);
    await component.refresh({} as never);
    expect(component.showAddModal()).toBe(false);
  });

  it('should change limit for configurations', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    await component.changeLimit(25);
    expect(component.listingOptionsUsers().limit).toBe(25);
    expect(component.listingOptionsUsers().page).toBe(1);
  });

  it('should change limit for subscribed users', async () => {
    paramsSubject.next({ id: 'ig1', nodeId: 'node1' });
    await new Promise((resolve) => setTimeout(resolve));

    await component.changeLimitResult(20);
    expect(component.listingOptionsUsers().limit).toBe(20);
    expect(component.listingOptionsUsers().page).toBe(1);
  });
});
