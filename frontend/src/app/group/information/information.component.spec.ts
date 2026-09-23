import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import {
  InformationService,
  NodesService,
  NotificationService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { InformationComponent } from './information.component';

const paramsSubject = new Subject<Record<string, string>>();
const queryParamsSubject = new Subject<Record<string, string>>();
const dataSubject = new Subject<Record<string, unknown>>();

const mockRoute = {
  data: dataSubject.asObservable(),
  params: paramsSubject.asObservable(),
  queryParams: queryParamsSubject.asObservable(),
  snapshot: { queryParams: {} },
};

const mockRouter = {
  navigate: vi.fn().mockResolvedValue(true),
};

const mockInformationService = {
  getInformationDefinitionsAsync: vi
    .fn()
    .mockResolvedValue({ permissions: {} }),
  getInformationNewsAsync: vi.fn().mockResolvedValue({ data: [], total: 0 }),
  getNewsAsync: vi.fn().mockResolvedValue({ id: 'n-remote' }),
};

const mockNodesService = {
  getNodeAsync: vi
    .fn()
    .mockResolvedValue({ id: 'info-node-1', notifications: 'ALLOWED' }),
};

const mockNotificationService = {
  putNotificationAuthority: vi.fn().mockReturnValue(of({})),
  putNotificationAuthorityAsync: vi.fn().mockResolvedValue({}),
};

const mockLoginService = {
  isGuest: vi.fn().mockReturnValue(false),
  getTicket: vi.fn().mockReturnValue('ticket123'),
  getCurrentUsername: vi.fn().mockReturnValue('testuser'),
};

const mockUiMessageService = {
  addErrorMessage: vi.fn(),
};

const mockTranslocoService = {
  translate: vi.fn().mockReturnValue('error'),
};

describe('InformationComponent', () => {
  let component: InformationComponent;
  let fixture: ComponentFixture<InformationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InformationComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: Router, useValue: mockRouter },
        { provide: InformationService, useValue: mockInformationService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: NotificationService, useValue: mockNotificationService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: TranslocoService, useValue: mockTranslocoService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(InformationComponent, {
        set: { schemas: [NO_ERRORS_SCHEMA], template: '' },
      })
      .compileComponents();

    fixture = TestBed.createComponent(InformationComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnInit', () => {
    it('should set group from route data', () => {
      const group = { id: 'g1', informationId: 'info1' };
      fixture.detectChanges();
      dataSubject.next({ group });
      expect(component.group()).toEqual(group);
    });
  });

  describe('loadInformation', () => {
    it('should load information when groupId and group.informationId exist', async () => {
      component.group.set({ id: 'g1', informationId: 'info1' } as never);

      await component.loadInformation({ id: 'g1' });

      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'info1',
      });
      expect(
        mockInformationService.getInformationDefinitionsAsync
      ).toHaveBeenCalledWith({ id: 'g1' });
      expect(
        mockInformationService.getInformationNewsAsync
      ).toHaveBeenCalledWith({
        id: 'g1',
        limit: 10,
        page: 1,
      });
      expect(component.loading()).toBe(false);
    });

    it('should not load if group has no informationId', async () => {
      component.group.set({ id: 'g1' } as never);
      mockNodesService.getNodeAsync.mockClear();

      await component.loadInformation({ id: 'g1' });

      expect(mockNodesService.getNodeAsync).not.toHaveBeenCalled();
      expect(component.loading()).toBe(false);
    });

    describe('with filterId query parameter', () => {
      afterEach(() => {
        mockRoute.snapshot.queryParams = {};
        mockInformationService.getInformationNewsAsync.mockResolvedValue({
          data: [],
          total: 0,
        });
        mockInformationService.getNewsAsync.mockResolvedValue({
          id: 'n-remote',
        });
      });

      it('should highlight the news item found in the current page', async () => {
        const news = { id: 'n1' };
        component.group.set({ id: 'g1', informationId: 'info1' } as never);
        mockRoute.snapshot.queryParams = { filterId: 'n1' };
        mockInformationService.getInformationNewsAsync.mockResolvedValue({
          data: [news],
          total: 1,
        });
        mockInformationService.getNewsAsync.mockClear();

        await component.loadInformation({ id: 'g1' });

        expect(component.highlightedMode()).toBe(true);
        expect(component.highlightedNews()).toBe(news);
        // No direct fetch needed when the item is on the current page.
        expect(mockInformationService.getNewsAsync).not.toHaveBeenCalled();
      });

      it('should fetch the news directly when not on the current page', async () => {
        const remoteNews = { id: 'n-remote' };
        component.group.set({ id: 'g1', informationId: 'info1' } as never);
        mockRoute.snapshot.queryParams = { filterId: 'n-remote' };
        mockInformationService.getInformationNewsAsync.mockResolvedValue({
          data: [{ id: 'n1' }],
          total: 30,
        });
        mockInformationService.getNewsAsync.mockResolvedValue(remoteNews);

        await component.loadInformation({ id: 'g1' });

        expect(component.highlightedMode()).toBe(true);
        expect(mockInformationService.getNewsAsync).toHaveBeenCalledWith({
          id: 'n-remote',
        });
        expect(component.highlightedNews()).toEqual(remoteNews);
      });

      it('should leave highlightedNews undefined when the direct fetch fails', async () => {
        component.group.set({ id: 'g1', informationId: 'info1' } as never);
        mockRoute.snapshot.queryParams = { filterId: 'missing' };
        mockInformationService.getInformationNewsAsync.mockResolvedValue({
          data: [{ id: 'n1' }],
          total: 1,
        });
        mockInformationService.getNewsAsync.mockRejectedValue(
          new Error('not found')
        );

        await component.loadInformation({ id: 'g1' });

        expect(component.highlightedMode()).toBe(true);
        expect(component.highlightedNews()).toBeUndefined();
      });
    });
  });

  describe('hasOldInformation', () => {
    it('should return true when url and displayOldInformation are set', () => {
      component.informationPage.set({
        url: 'http://example.com',
        displayOldInformation: true,
      });
      expect(component.hasOldInformation()).toBe(true);
    });

    it('should return false when url is missing', () => {
      component.informationPage.set({ displayOldInformation: true });
      expect(component.hasOldInformation()).toBe(false);
    });

    it('should return false when displayOldInformation is false', () => {
      component.informationPage.set({
        url: 'http://example.com',
        displayOldInformation: false,
      });
      expect(component.hasOldInformation()).toBe(false);
    });
  });

  describe('getOldInformation', () => {
    it('should return news with ticket appended when not guest', () => {
      component.informationPage.set({
        url: 'http://example.com',
        displayOldInformation: true,
      });
      mockLoginService.isGuest.mockReturnValue(false);

      const result = component.getOldInformation();

      expect(result).toBeDefined();
      expect(result?.url).toBe('http://example.com?ticket=ticket123');
      expect(result?.pattern).toBe('iframe');
    });

    it('should return undefined when conditions not met', () => {
      component.informationPage.set({ url: '', displayOldInformation: false });
      expect(component.getOldInformation()).toBeUndefined();
    });
  });

  describe('checkTicket', () => {
    it('should return empty string for guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.checkTicket()).toBe('');
    });

    it('should return ticket param for logged-in user', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      expect(component.checkTicket()).toBe('?ticket=ticket123');
    });
  });

  describe('hasCards', () => {
    it('should return true when total > 0', () => {
      component.infoNews.set({ data: [{}], total: 1 });
      expect(component.hasCards()).toBe(true);
    });

    it('should return false when total is 0', () => {
      component.infoNews.set({ data: [], total: 0 });
      expect(component.hasCards()).toBe(false);
    });
  });

  describe('canAddNews', () => {
    it('should return true when InfManage is ALLOWED', () => {
      component.informationPage.set({ permissions: { InfManage: 'ALLOWED' } });
      expect(component.canAddNews()).toBe(true);
    });

    it('should return true when InfFullEdit is ALLOWED', () => {
      component.informationPage.set({
        permissions: { InfFullEdit: 'ALLOWED' },
      });
      expect(component.canAddNews()).toBe(true);
    });

    it('should return false when no permissions', () => {
      component.informationPage.set({ permissions: {} });
      expect(component.canAddNews()).toBe(false);
    });

    it('should return false when informationPage has no permissions', () => {
      component.informationPage.set({});
      expect(component.canAddNews()).toBe(false);
    });
  });

  describe('isSubscribedToNotifications', () => {
    it('should return true when notifications is ALLOWED', () => {
      component.informationNode.set({ notifications: 'ALLOWED' } as never);
      expect(component.isSubscribedToNotifications()).toBe(true);
    });

    it('should return false when no informationNode', () => {
      component.informationNode.set(undefined);
      expect(component.isSubscribedToNotifications()).toBe(false);
    });
  });

  describe('isGuest', () => {
    it('should delegate to loginService', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isGuest()).toBe(true);
    });
  });

  describe('highlight', () => {
    it('should do nothing for undefined news', async () => {
      await component.highlight(undefined);
      expect(component.highlightedMode()).toBe(false);
    });

    it('should set highlighted mode for a news item', async () => {
      const news = { id: 'n1' };
      await component.highlight(news);
      expect(component.highlightedMode()).toBe(true);
      expect(component.highlightedNews()).toBe(news);
      expect(mockRouter.navigate).toHaveBeenCalled();
    });

    it('should reset when same news is highlighted again', async () => {
      const news = { id: 'n1' };
      component.highlightedNews.set(news);
      await component.highlight(news);
      expect(component.highlightedMode()).toBe(false);
      expect(component.highlightedNews()).toBeUndefined();
    });
  });

  describe('highlightIFrame', () => {
    it('should toggle iframe mode on', () => {
      component.highlightedIframeMode.set(false);
      component.highlightIFrame();
      expect(component.highlightedIframeMode()).toBe(true);
      expect(component.highlightedMode()).toBe(false);
    });

    it('should toggle iframe mode off', () => {
      component.highlightedIframeMode.set(true);
      component.highlightIFrame();
      expect(component.highlightedIframeMode()).toBe(false);
      expect(component.highlightedMode()).toBe(false);
    });
  });

  describe('maxWindowHighlighted', () => {
    it('should set highlightedMaxWindow', () => {
      component.maxWindowHighlighted(true);
      expect(component.highlightedMaxWindow).toBe(true);
    });
  });

  describe('refresh', () => {
    it('should reload information on DELETE_INFORMATION_NEWS success', async () => {
      component.groupId.set('g1');
      component.group.set({ id: 'g1', informationId: 'info1' } as never);

      await component.refresh({
        type: ActionType.DELETE_INFORMATION_NEWS,
        result: ActionResult.SUCCEED,
      });

      expect(component.highlightedMode()).toBe(false);
      expect(component.highlightedNews()).toBeUndefined();
      expect(mockInformationService.getInformationNewsAsync).toHaveBeenCalled();
    });

    it('should reload definitions on UPDATE_INFORMATION_CONFIGURATION success', async () => {
      component.groupId.set('g1');
      mockInformationService.getInformationDefinitionsAsync.mockClear();

      await component.refresh({
        type: ActionType.UPDATE_INFORMATION_CONFIGURATION,
        result: ActionResult.SUCCEED,
      });

      expect(
        mockInformationService.getInformationDefinitionsAsync
      ).toHaveBeenCalledWith({ id: 'g1' });
    });

    it('should close modal on any refresh', async () => {
      component.showConfigureModal = true;
      await component.refresh({
        type: ActionType.CREATE_SPACE,
        result: ActionResult.CANCELED,
      });
      expect(component.showConfigureModal).toBe(false);
    });
  });

  describe('changePage', () => {
    it('should update page and reload', async () => {
      component.groupId.set('g1');
      component.group.set({ id: 'g1', informationId: 'info1' } as never);

      await component.changePage(3);

      expect(component.listingOptions.page).toBe(3);
      expect(mockInformationService.getInformationNewsAsync).toHaveBeenCalled();
    });
  });

  describe('changeNotificationSubscription', () => {
    it('should call putNotificationAuthority and reload node', async () => {
      component.informationNode.set({
        id: 'info-node-1',
        notifications: 'ALLOWED',
      } as never);

      await component.changeNotificationSubscription('ALLOWED');

      expect(
        mockNotificationService.putNotificationAuthorityAsync
      ).toHaveBeenCalledWith({
        id: 'info-node-1',
        authority: 'testuser',
        body: 'ALLOWED',
      });
      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'info-node-1',
      });
    });

    it('should show error message on failure', async () => {
      component.informationNode.set({
        id: 'info-node-1',
        notifications: 'ALLOWED',
      } as never);
      mockNotificationService.putNotificationAuthorityAsync.mockRejectedValueOnce(
        new Error('fail')
      );

      await component.changeNotificationSubscription('ALLOWED');

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    });

    it('should do nothing for empty value', async () => {
      component.informationNode.set({
        id: 'info-node-1',
        notifications: 'ALLOWED',
      } as never);
      mockNotificationService.putNotificationAuthorityAsync.mockClear();

      await component.changeNotificationSubscription('');

      expect(
        mockNotificationService.putNotificationAuthorityAsync
      ).not.toHaveBeenCalled();
    });
  });
});
