import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  GroupConfiguration,
  InterestGroup,
  InterestGroupService,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { ForumComponent } from './forum.component';

const mockNode: ModelNode = {
  id: 'node-1',
  type: 'ci:circaForums',
  permissions: { newsgroup: 'NwsPost' },
};

const mockGroup: InterestGroup = {
  id: 'group-1',
  name: 'Test Group',
  newsgroupId: 'newsgroup-1',
  permissions: { newsgroup: 'NwsPost', library: 'LibAccess' },
};

const mockGroupConfiguration: GroupConfiguration = {
  newsgroups: {
    enableFlagNewTopic: true,
    enableFlagNewForum: true,
    ageFlagNewTopic: 5,
    ageFlagNewForum: 5,
  },
};

describe('ForumComponent', () => {
  let component: ForumComponent;

  const paramsSubject = new BehaviorSubject<{ nodeId: string }>({
    nodeId: 'node-1',
  });
  const dataSubject = new Subject<{ group: InterestGroup }>();

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue(mockNode),
    getPath: vi.fn().mockReturnValue(of([{ id: 'root' }, mockNode])),
    getPathAsync: vi.fn().mockResolvedValue([{ id: 'root' }, mockNode]),
  };

  const mockGroupsService = {
    getGroupConfigurationAsync: vi
      .fn()
      .mockResolvedValue(mockGroupConfiguration),
  };

  const mockPermEvalService = {
    isNewsgroupPost: vi.fn().mockReturnValue(false),
    isNewsgroupModerate: vi.fn().mockReturnValue(false),
    isNewsgroupAdmin: vi.fn().mockReturnValue(false),
    isOwner: vi.fn().mockReturnValue(false),
    isLibAdmin: vi.fn().mockReturnValue(false),
  };

  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('testuser'),
  };

  const mockRouter = {
    navigate: vi.fn(),
  };

  const mockRoute = {
    data: dataSubject.asObservable(),
    params: paramsSubject.asObservable(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ForumComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: NodesService, useValue: mockNodesService },
        { provide: InterestGroupService, useValue: mockGroupsService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockRoute },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(ForumComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnInit', () => {
    it('should load forum node when route params emit', async () => {
      component.ngOnInit();
      dataSubject.next({ group: mockGroup });

      await vi.waitFor(() => {
        expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
          id: 'node-1',
        });
      });
      expect(component.node()).toEqual(mockNode);
      expect(component.loading()).toBe(false);
    });

    it('should set group and groupId from route data', () => {
      component.ngOnInit();
      dataSubject.next({ group: mockGroup });

      expect(component.group()).toEqual(mockGroup);
      expect(component.groupId()).toBe('group-1');
    });

    it('should fetch group configuration when newsgroupId exists', async () => {
      component.ngOnInit();
      dataSubject.next({ group: mockGroup });

      await vi.waitFor(() => {
        expect(
          mockGroupsService.getGroupConfigurationAsync
        ).toHaveBeenCalledWith({
          id: 'group-1',
        });
      });
    });
  });

  describe('refresh', () => {
    it('should navigate to new forum on CREATE_FORUM success', async () => {
      await component.refresh({
        result: ActionResult.SUCCEED,
        type: ActionType.CREATE_FORUM,
        node: { id: 'new-forum-id' },
      });

      expect(mockRouter.navigate).toHaveBeenCalledWith(['..', 'new-forum-id'], {
        relativeTo: mockRoute,
      });
    });

    it('should reload forum on CREATE_TOPIC success', async () => {
      component.nodeId = 'node-1';
      mockNodesService.getNodeAsync.mockClear();

      await component.refresh({
        result: ActionResult.SUCCEED,
        type: ActionType.CREATE_TOPIC,
      });

      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'node-1',
      });
    });

    it('should do nothing on CANCELED result', async () => {
      mockRouter.navigate.mockClear();
      mockNodesService.getNodeAsync.mockClear();

      await component.refresh({
        result: ActionResult.CANCELED,
        type: ActionType.CREATE_FORUM,
      });

      expect(mockRouter.navigate).not.toHaveBeenCalled();
      expect(mockNodesService.getNodeAsync).not.toHaveBeenCalled();
    });
  });

  describe('loadForum error handling', () => {
    it('should reset loading and keep previous node when the load fails', async () => {
      mockNodesService.getNodeAsync.mockRejectedValueOnce(
        new Error('network error')
      );

      await component.loadForum({ nodeId: 'node-err' });

      expect(component.loading()).toBe(false);
      expect(component.node()).toBeUndefined();
    });
  });

  describe('isNewsgroupPost', () => {
    it('should return true when user has post or moderate permission', () => {
      component.node.set(mockNode);
      mockPermEvalService.isNewsgroupPost.mockReturnValue(true);

      expect(component.isNewsgroupPost()).toBe(true);
    });

    it('should return false when user has no post permission', () => {
      component.node.set(mockNode);
      mockPermEvalService.isNewsgroupPost.mockReturnValue(false);
      mockPermEvalService.isNewsgroupModerate.mockReturnValue(false);

      expect(component.isNewsgroupPost()).toBe(false);
    });
  });

  describe('isNewsgroupModerateAdmin', () => {
    it('should return true when user is newsgroup admin', () => {
      component.node.set(mockNode);
      mockPermEvalService.isNewsgroupAdmin.mockReturnValue(true);

      expect(component.isNewsgroupModerateAdmin()).toBe(true);
    });

    it('should return false when user has no admin or moderate permission', () => {
      component.node.set(mockNode);
      component.group.set({
        ...mockGroup,
        permissions: { newsgroup: 'NwsAccess', library: 'LibAccess' },
      });
      mockPermEvalService.isNewsgroupAdmin.mockReturnValue(false);
      mockPermEvalService.isNewsgroupModerate.mockReturnValue(false);
      mockPermEvalService.isOwner.mockReturnValue(false);

      expect(component.isNewsgroupModerateAdmin()).toBe(false);
    });
  });

  describe('isNewsgroupRoot', () => {
    it('should return true when node type ends with forums', () => {
      component.node.set({ ...mockNode, type: 'ci:circaforums' });
      expect(component.isNewsgroupRoot()).toBe(true);
    });

    it('should return false when node type does not end with forums', () => {
      component.node.set({ ...mockNode, type: 'ci:circaForum' });
      expect(component.isNewsgroupRoot()).toBe(false);
    });
  });

  describe('isNewsgroupAdmin', () => {
    it('should return true when group permission is NwsAdmin', () => {
      component.node.set(mockNode);
      component.group.set({
        ...mockGroup,
        permissions: { newsgroup: 'NwsAdmin', library: 'LibAccess' },
      });

      expect(component.isNewsgroupAdmin()).toBe(true);
    });

    it('should return true when permEvalService says admin', () => {
      component.node.set(mockNode);
      component.group.set(mockGroup);
      mockPermEvalService.isNewsgroupAdmin.mockReturnValue(true);

      expect(component.isNewsgroupAdmin()).toBe(true);
    });

    it('should return true when user is owner', () => {
      component.node.set(mockNode);
      component.group.set(mockGroup);
      mockPermEvalService.isNewsgroupAdmin.mockReturnValue(false);
      mockPermEvalService.isOwner.mockReturnValue(true);

      expect(component.isNewsgroupAdmin()).toBe(true);
    });
  });

  describe('isLibAdmin', () => {
    it('should return true when group permission is LibAdmin', () => {
      component.node.set(mockNode);
      component.group.set({
        ...mockGroup,
        permissions: { newsgroup: 'NwsPost', library: 'LibAdmin' },
      });

      expect(component.isLibAdmin()).toBe(true);
    });

    it('should return true when permEvalService says lib admin', () => {
      component.node.set(mockNode);
      component.group.set(mockGroup);
      mockPermEvalService.isLibAdmin.mockReturnValue(true);

      expect(component.isLibAdmin()).toBe(true);
    });

    it('should return false when no lib admin permission', () => {
      component.node.set(mockNode);
      component.group.set(mockGroup);
      mockPermEvalService.isLibAdmin.mockReturnValue(false);

      expect(component.isLibAdmin()).toBe(false);
    });
  });

  describe('refreshConf', () => {
    it('should reload group configuration on success', async () => {
      component.groupId.set('group-1');
      mockGroupsService.getGroupConfigurationAsync.mockClear();

      await component.refreshConf({ result: ActionResult.SUCCEED });

      expect(mockGroupsService.getGroupConfigurationAsync).toHaveBeenCalledWith(
        {
          id: 'group-1',
        }
      );
      expect(component.groupConfiguration()).toEqual(mockGroupConfiguration);
    });

    it('should not reload on failure', async () => {
      component.groupId.set('group-1');
      mockGroupsService.getGroupConfigurationAsync.mockClear();

      await component.refreshConf({ result: ActionResult.FAILED });

      expect(
        mockGroupsService.getGroupConfigurationAsync
      ).not.toHaveBeenCalled();
    });
  });
});
