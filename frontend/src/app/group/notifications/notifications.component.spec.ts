import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import type { ActionEmitterResult } from 'app/action-result';
import { ActionResult, ActionType } from 'app/action-result';
import {
  Node as ModelNode,
  NodesService,
  NotificationDefinition,
  NotificationService,
} from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { NotificationsComponent } from './notifications.component';

describe('NotificationsComponent', () => {
  let component: NotificationsComponent;

  let paramsSubject: Subject<Record<string, string>>;
  let queryParamsSubject: Subject<Record<string, string>>;

  const mockNode: ModelNode = {
    id: 'node-1',
    parentId: 'parent-1',
    name: 'Test Node',
  };
  const mockNotifs: NotificationDefinition = { profiles: [], users: [] };

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue(mockNode),
  };

  const mockNotificationService = {
    getNotificationsAsync: vi.fn().mockResolvedValue(mockNotifs),
    deleteNotificationAuthority: vi.fn().mockReturnValue(of(undefined)),
    deleteNotificationAuthorityAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  const mockRouter = {
    navigate: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    paramsSubject = new Subject();
    queryParamsSubject = new Subject();

    await TestBed.configureTestingModule({
      imports: [NotificationsComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            params: paramsSubject.asObservable(),
            queryParams: queryParamsSubject.asObservable(),
          },
        },
        { provide: NodesService, useValue: mockNodesService },
        { provide: NotificationService, useValue: mockNotificationService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: Router, useValue: mockRouter },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(NotificationsComponent, {
        set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    const fixture = TestBed.createComponent(NotificationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnInit', () => {
    it('should load node when params emit with nodeId', async () => {
      paramsSubject.next({ id: 'ig-1', nodeId: 'node-1' });
      await new Promise((r) => setTimeout(r, 0));

      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'node-1',
      });
      expect(
        mockNotificationService.getNotificationsAsync
      ).toHaveBeenCalledWith({
        id: 'node-1',
      });
      expect(component.currentIg()).toBe('ig-1');
      expect(component.loading()).toBe(false);
    });

    it('should set from when queryParams emit', () => {
      queryParamsSubject.next({ from: 'library' });
      expect(component.from()).toBe('library');
    });
  });

  describe('deleteNotification', () => {
    beforeEach(() => {
      component.currentNode.set(mockNode);
    });

    it('should delete notification and reload', async () => {
      await component.deleteNotification('user-1');

      expect(
        mockNotificationService.deleteNotificationAuthorityAsync
      ).toHaveBeenCalledWith({ id: 'node-1', authority: 'user-1' });
      expect(
        mockNotificationService.getNotificationsAsync
      ).toHaveBeenCalledWith({
        id: 'node-1',
      });
    });

    it('should not call service if authority is undefined', async () => {
      await component.deleteNotification(undefined);

      expect(
        mockNotificationService.deleteNotificationAuthorityAsync
      ).not.toHaveBeenCalled();
    });

    it('should show error message on failure', async () => {
      mockNotificationService.deleteNotificationAuthorityAsync.mockRejectedValueOnce(
        new Error('fail')
      );

      await component.deleteNotification('user-1');

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    });
  });

  describe('refresh', () => {
    beforeEach(() => {
      component.currentNode.set(mockNode);
      component.showAddModal.set(true);
    });

    it('should reload notifications and close modal on success', async () => {
      const result: ActionEmitterResult = {
        result: ActionResult.SUCCEED,
        type: ActionType.ADD_NOTIFICATIONS,
      };

      await component.refresh(result);

      expect(
        mockNotificationService.getNotificationsAsync
      ).toHaveBeenCalledWith({
        id: 'node-1',
      });
      expect(component.showAddModal()).toBe(false);
    });

    it('should close modal on cancel', async () => {
      const result: ActionEmitterResult = {
        result: ActionResult.CANCELED,
        type: ActionType.ADD_NOTIFICATIONS,
      };

      await component.refresh(result);

      expect(component.showAddModal()).toBe(false);
    });

    it('should not close modal for other action types', async () => {
      const result: ActionEmitterResult = {
        result: ActionResult.SUCCEED,
        type: ActionType.DELETE_NOTIFICATION,
      };

      await component.refresh(result);

      expect(component.showAddModal()).toBe(true);
    });
  });

  describe('goBack', () => {
    beforeEach(() => {
      component.currentNode.set(mockNode);
    });

    it('should navigate to library details when from is library', async () => {
      component.from.set('library');
      await component.goBack();

      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['../../library', 'node-1', 'details'],
        expect.objectContaining({ relativeTo: expect.anything() })
      );
    });

    it('should navigate to forum when from is forum', async () => {
      component.from.set('forum');
      await component.goBack();

      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['../../forum', 'node-1'],
        expect.objectContaining({ relativeTo: expect.anything() })
      );
    });

    it('should navigate to topic when from is topic', async () => {
      component.from.set('topic');
      await component.goBack();

      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['../../forum/topic', 'node-1'],
        expect.objectContaining({ relativeTo: expect.anything() })
      );
    });
  });

  describe('goBackToFolder', () => {
    it('should navigate to parent folder', async () => {
      component.currentNode.set(mockNode);
      await component.goBackToFolder();

      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['../../library', 'parent-1'],
        expect.objectContaining({ relativeTo: expect.anything() })
      );
    });
  });

  describe('getNodeId', () => {
    it('should return node id when currentNode exists', () => {
      component.currentNode.set(mockNode);
      expect(component.getNodeId()).toBe('node-1');
    });

    it('should return empty string when currentNode is undefined', () => {
      component.currentNode.set(undefined);
      expect(component.getNodeId()).toBe('');
    });
  });
});
