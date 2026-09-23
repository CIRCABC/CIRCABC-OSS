import { ComponentRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  ForumService,
  Node as ModelNode,
  NodesService,
  NotificationService,
  PagedNodes,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { ForumBrowserComponent } from './forum-browser.component';

const mockForum: ModelNode = {
  id: 'forum-1',
  name: 'Test Forum',
  type: '{http://www.alfresco.org/model/forum/1.0}forum',
  parentId: 'parent-1',
  notifications: 'ALLOWED',
};

const mockPagedNodes: PagedNodes = {
  data: [
    { id: 'topic-1', name: 'Topic 1', type: 'fm:topic' },
    { id: 'subforum-1', name: 'Sub Forum', type: 'fm:forum' },
  ],
  total: 2,
};

const mockForumService = {
  getForumContentAsync: vi.fn().mockResolvedValue(mockPagedNodes),
  getSubforums: vi.fn().mockReturnValue(of([])),
  getSubforumsAsync: vi.fn().mockResolvedValue([]),
};

const mockNodesService = {
  getPathAsync: vi.fn().mockResolvedValue([{ id: 'root' }, mockForum]),
  getNodeAsync: vi.fn().mockResolvedValue(mockForum),
};

const mockNotificationService = {
  putNotificationAuthority: vi.fn().mockReturnValue(of(undefined)),
  putNotificationAuthorityAsync: vi.fn().mockResolvedValue(undefined),
};

const mockLoginService = {
  isGuest: vi.fn().mockReturnValue(false),
  getCurrentUsername: vi.fn().mockReturnValue('testuser'),
};

const mockPermEvalService = {
  isNewsgroupAdmin: vi.fn().mockReturnValue(false),
  isNewsgroupModerate: vi.fn().mockReturnValue(false),
};

const mockI18nPipe = { transform: vi.fn().mockReturnValue('') };

const actionFinished$ = new Subject<ActionEmitterResult>();
const mockActionService = {
  actionFinished$: actionFinished$.asObservable(),
};

const mockRouter = {
  navigate: vi.fn().mockReturnValue(Promise.resolve(true)),
  url: '',
};

const mockRoute = {};

describe('ForumBrowserComponent', () => {
  let component: ForumBrowserComponent;
  let componentRef: ComponentRef<ForumBrowserComponent>;
  let fixture: ComponentFixture<ForumBrowserComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      imports: [ForumBrowserComponent],
      providers: [
        { provide: ForumService, useValue: mockForumService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: NotificationService, useValue: mockNotificationService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: ActionService, useValue: mockActionService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockRoute },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ForumBrowserComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;

    componentRef.setInput('forum', mockForum);
    componentRef.setInput('group', {
      name: 'Test Group',
      newsgroupId: 'nws-root',
      permissions: { newsgroup: 'NwsAccess' },
    });
    componentRef.setInput('groupConfiguration', {
      newsgroups: {
        enableFlagNewTopic: false,
        enableFlagNewForum: false,
        ageFlagNewTopic: 7,
        ageFlagNewForum: 7,
      },
    });

    fixture.detectChanges();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('extractData', () => {
    it('should separate forums and topics from content', async () => {
      await new Promise((resolve) => setTimeout(resolve));
      expect(component.topics()!).toHaveLength(1);
      expect(component.forums()!).toHaveLength(1);
      expect(component.topics()?.[0].id).toBe('topic-1');
      expect(component.forums()?.[0].id).toBe('subforum-1');
    });
  });

  describe('isGuest', () => {
    it('should return false when user is logged in', () => {
      expect(component.isGuest()).toBe(false);
    });

    it('should return true when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isGuest()).toBe(true);
    });
  });

  describe('isSubscribedToNotifications', () => {
    it('should return true when notifications is ALLOWED', () => {
      expect(component.isSubscribedToNotifications()).toBe(true);
    });

    it('should return false when notifications is not ALLOWED', () => {
      componentRef.setInput('forum', {
        ...mockForum,
        notifications: 'REJECTED',
      });
      expect(component.isSubscribedToNotifications()).toBe(false);
    });
  });

  describe('isForumRoot', () => {
    it('should return false for a regular forum', () => {
      expect(component.isForumRoot()).toBe(false);
    });

    it('should return true for root forums type', () => {
      componentRef.setInput('forum', {
        ...mockForum,
        type: '{http://www.alfresco.org/model/forum/1.0}forums',
      });
      expect(component.isForumRoot()).toBe(true);
    });
  });

  describe('isNwsRoot', () => {
    it('should return true when forum name is Newsgroups', () => {
      expect(component.isNwsRoot({ name: 'Newsgroups' })).toBe(true);
    });

    it('should return false for other names', () => {
      expect(component.isNwsRoot(mockForum)).toBe(false);
    });
  });

  describe('hasSubForums', () => {
    it('should return true when forums exist', async () => {
      await new Promise((resolve) => setTimeout(resolve));
      expect(component.hasSubForums()).toBe(true);
    });

    it('should return false when no forums', () => {
      component.forums.set([]);
      expect(component.hasSubForums()).toBe(false);
    });
  });

  describe('nameExists', () => {
    it('should return true when name is defined and non-empty', () => {
      expect(component.nameExists({ name: 'test' })).toBe(true);
    });

    it('should return false when name is empty', () => {
      expect(component.nameExists({ name: '' })).toBe(false);
    });

    it('should return false when name is undefined', () => {
      expect(component.nameExists({})).toBe(false);
    });
  });

  describe('isForum', () => {
    it('should return true for forum type', () => {
      expect(component.isForum({ type: 'fm:forum' })).toBe(true);
    });

    it('should return false for topic type', () => {
      expect(component.isForum({ type: 'fm:topic' })).toBe(false);
    });

    it('should return false when type is undefined', () => {
      expect(component.isForum({})).toBe(false);
    });
  });

  describe('modified', () => {
    it('should return modified property', () => {
      expect(
        component.modified({ properties: { modified: '2026-01-01' } })
      ).toBe('2026-01-01');
    });

    it('should return null when no properties', () => {
      expect(component.modified({})).toBeNull();
    });
  });

  describe('creator', () => {
    it('should return creator property', () => {
      expect(component.creator({ properties: { creator: 'admin' } })).toBe(
        'admin'
      );
    });

    it('should return null when no properties', () => {
      expect(component.creator({})).toBeNull();
    });
  });

  describe('isNewTopic', () => {
    it('should return false when enableFlagNewTopic is false', () => {
      expect(
        component.isNewTopic({
          properties: { created: new Date().toISOString() },
        })
      ).toBe(false);
    });

    it('should return true for recent topic when flag enabled', () => {
      componentRef.setInput('groupConfiguration', {
        newsgroups: {
          enableFlagNewTopic: true,
          enableFlagNewForum: false,
          ageFlagNewTopic: 7,
          ageFlagNewForum: 7,
        },
      });
      expect(
        component.isNewTopic({
          properties: { created: new Date().toISOString() },
        })
      ).toBe(true);
    });

    it('should return false for old topic when flag enabled', () => {
      componentRef.setInput('groupConfiguration', {
        newsgroups: {
          enableFlagNewTopic: true,
          enableFlagNewForum: false,
          ageFlagNewTopic: 7,
          ageFlagNewForum: 7,
        },
      });
      const oldDate = new Date();
      oldDate.setDate(oldDate.getDate() - 30);
      expect(
        component.isNewTopic({ properties: { created: oldDate.toISOString() } })
      ).toBe(false);
    });
  });

  describe('isNewForum', () => {
    it('should return false when enableFlagNewForum is false', () => {
      expect(
        component.isNewForum({
          properties: { created: new Date().toISOString() },
        })
      ).toBe(false);
    });

    it('should return true for recent forum when flag enabled', () => {
      componentRef.setInput('groupConfiguration', {
        newsgroups: {
          enableFlagNewTopic: false,
          enableFlagNewForum: true,
          ageFlagNewTopic: 7,
          ageFlagNewForum: 7,
        },
      });
      expect(
        component.isNewForum({
          properties: { created: new Date().toISOString() },
        })
      ).toBe(true);
    });
  });

  describe('isNewsgroupAdmin', () => {
    it('should return false when user has no admin permission', () => {
      expect(component.isNewsgroupAdmin()).toBe(false);
    });

    it('should return true when group permission is NwsAdmin', () => {
      componentRef.setInput('group', {
        name: 'Test Group',
        newsgroupId: 'nws-root',
        permissions: { newsgroup: 'NwsAdmin' },
      });
      expect(component.isNewsgroupAdmin()).toBe(true);
    });
  });

  describe('changeNotificationSubscription', () => {
    it('should call notification service with correct params', async () => {
      await component.changeNotificationSubscription('ALLOWED');

      expect(
        mockNotificationService.putNotificationAuthorityAsync
      ).toHaveBeenCalledWith({
        id: 'forum-1',
        authority: 'testuser',
        body: 'ALLOWED',
      });
    });

    it('should not call service when value is empty', async () => {
      await component.changeNotificationSubscription('');
      expect(
        mockNotificationService.putNotificationAuthorityAsync
      ).not.toHaveBeenCalled();
    });
  });

  describe('changePage', () => {
    it('should update page and reload content', async () => {
      mockForumService.getForumContentAsync.mockClear();
      await component.changePage(2);

      expect(component.listingOptions().page).toBe(2);
      expect(mockForumService.getForumContentAsync).toHaveBeenCalled();
    });
  });

  describe('changeLimit', () => {
    it('should update limit, reset page to 1, and reload', async () => {
      component.listingOptions.set({ ...component.listingOptions(), page: 3 });
      mockForumService.getForumContentAsync.mockClear();
      await component.changeLimit(25);

      expect(component.listingOptions().limit).toBe(25);
      expect(component.listingOptions().page).toBe(1);
      expect(mockForumService.getForumContentAsync).toHaveBeenCalled();
    });
  });

  describe('prepareTopicDeletion', () => {
    it('should set topic and show modal', () => {
      const topic: ModelNode = { id: 'topic-1', name: 'Topic' };
      component.prepareTopicDeletion(topic);
      expect(component.currentDeletedTopic).toBe(topic);
      expect(component.showDeleteTopic()).toBe(true);
    });
  });

  describe('refreshDeleteTopic', () => {
    it('should hide modal on success', async () => {
      component.showDeleteTopic.set(true);
      await component.refreshDeleteTopic({
        result: ActionResult.SUCCEED,
        type: ActionType.DELETE_TOPIC,
      });
      expect(component.showDeleteTopic()).toBe(false);
    });

    it('should hide modal on failure', async () => {
      component.showDeleteTopic.set(true);
      await component.refreshDeleteTopic({
        result: ActionResult.FAILED,
        type: ActionType.DELETE_TOPIC,
      });
      expect(component.showDeleteTopic()).toBe(false);
    });

    it('should hide modal on cancel', async () => {
      component.showDeleteTopic.set(true);
      await component.refreshDeleteTopic({
        result: ActionResult.CANCELED,
        type: ActionType.DELETE_TOPIC,
      });
      expect(component.showDeleteTopic()).toBe(false);
    });
  });

  describe('prepareForumDeletion', () => {
    it('should set forum and show modal', () => {
      const forum: ModelNode = { id: 'f-1', name: 'Forum' };
      component.prepareForumDeletion(forum);
      expect(component.currentDeletedForum).toBe(forum);
      expect(component.showDeleteForum()).toBe(true);
    });
  });

  describe('propagateNavigation', () => {
    it('should navigate to the node id', async () => {
      await component.propagateNavigation({ nodeId: 'node-123' });
      expect(component.searchedNodeId).toBe('node-123');
      expect(mockRouter.navigate).toHaveBeenCalledWith(['..', 'node-123'], {
        relativeTo: mockRoute,
      });
    });
  });

  describe('refreshModerateForum', () => {
    it('should hide moderation modal', async () => {
      component.showModerateForum = true;
      await component.refreshModerateForum({
        result: ActionResult.SUCCEED,
        type: ActionType.MODERATE_FORUM,
      } as ActionEmitterResult);
      expect(component.showModerateForum).toBe(false);
    });
  });
});
