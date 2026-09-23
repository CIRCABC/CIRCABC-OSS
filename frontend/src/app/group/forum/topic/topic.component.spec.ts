import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  InterestGroup,
  Node as ModelNode,
  NodesService,
  NotificationService,
  PostService,
  TopicService,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { TopicComponent } from './topic.component';

const mockGroup: InterestGroup = {
  name: 'Test Group',
  permissions: { newsgroup: 'NwsPost' },
};

const mockTopicNode: ModelNode = {
  id: 'topic-1',
  name: 'Test Topic',
  notifications: 'ALLOWED',
  properties: { message: 'hello', owner: 'admin' },
  attachments: [],
};

const mockPagedNodes = { data: [], total: 0 };

const paramsSubject = new Subject<{ [key: string]: string }>();
const dataSubject = new Subject<{ group: InterestGroup }>();

const mockRoute = {
  params: paramsSubject.asObservable(),
  data: dataSubject.asObservable(),
};

const mockNodesService = {
  getNodeAsync: vi.fn().mockResolvedValue(mockTopicNode),
};

const mockTopicService = {
  getRepliesAsync: vi.fn().mockResolvedValue(mockPagedNodes),
  postReplyAsync: vi.fn().mockResolvedValue({}),
};

const mockPostService = {
  getAttachmentsRemainingSizeAsync: vi
    .fn()
    .mockResolvedValue({ remainingSize: 10485760 }),
  putPostAsync: vi.fn().mockResolvedValue({}),
};

const mockLoginService = {
  isGuest: vi.fn().mockReturnValue(false),
  getCurrentUsername: vi.fn().mockReturnValue('testuser'),
};

const mockNotificationService = {
  putNotificationAuthorityAsync: vi.fn().mockResolvedValue({}),
};

const mockUiMessageService = {
  addSuccessMessage: vi.fn(),
  addErrorMessage: vi.fn(),
};

const mockPermEvalService = {
  isLibAdmin: vi.fn().mockReturnValue(false),
  isNewsgroupAdmin: vi.fn().mockReturnValue(false),
  isOwner: vi.fn().mockReturnValue(false),
};

const mockUserService = {
  getUser: vi.fn().mockReturnValue(of({ firstname: 'John', lastname: 'Doe' })),
};

describe('TopicComponent', () => {
  let component: TopicComponent;
  let fixture: ComponentFixture<TopicComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopicComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: NodesService, useValue: mockNodesService },
        { provide: TopicService, useValue: mockTopicService },
        { provide: PostService, useValue: mockPostService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: NotificationService, useValue: mockNotificationService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: UserService, useValue: mockUserService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TopicComponent);
    component = fixture.componentInstance;
    component.group.set(mockGroup);
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnInit', () => {
    it('should set group from route data', () => {
      fixture.detectChanges();
      dataSubject.next({ group: mockGroup });
      expect(component.group()).toEqual(mockGroup);
    });

    it('should load topic when params emit', async () => {
      fixture.detectChanges();
      dataSubject.next({ group: mockGroup });
      paramsSubject.next({ nodeId: 'topic-1' });
      await vi.waitFor(() => {
        expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
          id: 'topic-1',
        });
      });
    });
  });

  describe('loadTopic', () => {
    it('should fetch node and replies', async () => {
      await component.loadTopic({ nodeId: 'topic-1' });
      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'topic-1',
      });
      expect(mockTopicService.getRepliesAsync).toHaveBeenCalledWith({
        id: 'topic-1',
        limit: 10,
        page: 1,
        order: '',
      });
      expect(component.loading()).toBe(false);
    });

    it('should handle error when fetching replies', async () => {
      mockTopicService.getRepliesAsync.mockRejectedValueOnce(new Error('fail'));
      await component.loadTopic({ nodeId: 'topic-1' });
      expect(component.posts()).toEqual({ data: [], total: 0 });
    });
  });

  describe('isGuest', () => {
    it('should delegate to loginService', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isGuest()).toBe(true);
    });
  });

  describe('isSubscribedToNotifications', () => {
    it('should return true when notifications is ALLOWED', () => {
      component.topicNode.set({ ...mockTopicNode, notifications: 'ALLOWED' });
      expect(component.isSubscribedToNotifications()).toBe(true);
    });

    it('should return false when notifications is not ALLOWED', () => {
      component.topicNode.set({ ...mockTopicNode, notifications: 'DENIED' });
      expect(component.isSubscribedToNotifications()).toBe(false);
    });

    it('should return false when notifications is undefined', () => {
      component.topicNode.set({ ...mockTopicNode, notifications: undefined });
      expect(component.isSubscribedToNotifications()).toBe(false);
    });
  });

  describe('isNewsgroupAdmin', () => {
    beforeEach(() => {
      component.topicNode.set(mockTopicNode);
      component.group.set(mockGroup);
    });

    it('should return true when group permission is NwsAdmin', () => {
      component.group.set({
        ...mockGroup,
        permissions: { newsgroup: 'NwsAdmin' },
      });
      expect(component.isNewsgroupAdmin()).toBe(true);
    });

    it('should return true when permEvalService says admin', () => {
      mockPermEvalService.isNewsgroupAdmin.mockReturnValue(true);
      expect(component.isNewsgroupAdmin()).toBe(true);
      mockPermEvalService.isNewsgroupAdmin.mockReturnValue(false);
    });

    it('should return false when not admin and not owner', () => {
      mockPermEvalService.isNewsgroupAdmin.mockReturnValue(false);
      mockPermEvalService.isOwner.mockReturnValue(false);
      expect(component.isNewsgroupAdmin()).toBe(false);
    });
  });

  describe('nameExists', () => {
    it('should return true when node has a name', () => {
      expect(component.nameExists({ name: 'test' })).toBe(true);
    });

    it('should return false when node is undefined', () => {
      expect(component.nameExists(undefined as unknown as ModelNode)).toBe(
        false
      );
    });

    it('should return false when name is empty', () => {
      expect(component.nameExists({ name: '' })).toBe(false);
    });
  });

  describe('displayName', () => {
    it('should return empty string for undefined', () => {
      expect(component.displayName(undefined)).toBe('');
    });

    it('should return text as-is if 7 chars or less', () => {
      expect(component.displayName('short')).toBe('short');
    });

    it('should truncate text longer than 7 chars', () => {
      expect(component.displayName('longername')).toBe('longern...');
    });
  });

  describe('changePage', () => {
    it('should update page and reload topic', async () => {
      component.topicNode.set(mockTopicNode);
      await component.changePage(3);
      expect(component.listingOptions.page).toBe(3);
    });
  });

  describe('isAllValid', () => {
    it('should return false when remaining size is 0', () => {
      component.attachmentRemainingSize.set({ remainingSize: 0 });
      component.isValid = true;
      expect(component.isAllValid()).toBe(false);
    });

    it('should return true when valid and remaining size > 0', () => {
      component.attachmentRemainingSize.set({ remainingSize: 1000 });
      component.isValid = true;
      expect(component.isAllValid()).toBe(true);
    });

    it('should return true when files to upload exist', () => {
      component.attachmentRemainingSize.set({ remainingSize: 1000 });
      component.isValid = false;
      component.filesToUpload.set([
        { id: 1, file: new File([''], 'test.txt') },
      ]);
      expect(component.isAllValid()).toBe(true);
    });
  });

  describe('getRemainingSizeInMB', () => {
    it('should return size in MB with 2 decimals', () => {
      component.attachmentRemainingSize.set({ remainingSize: 10485760 });
      expect(component.getRemainingSizeInMB()).toBe('10.00');
    });

    it('should return 0 when attachmentRemainingSize is undefined', () => {
      component.attachmentRemainingSize.set(
        undefined as unknown as {
          remainingSize: number;
        }
      );
      expect(component.getRemainingSizeInMB()).toBe(0);
    });
  });

  describe('resetPost', () => {
    it('should reset all post-related state', () => {
      fixture.detectChanges();
      dataSubject.next({ group: mockGroup });
      component.postOpen.set(true);
      component.filesToUpload.set([{ id: 1, file: new File([''], 'f.txt') }]);
      component.pickedNodes = ['node1'];
      component.resetPost();
      expect(component.postOpen()).toBe(false);
      expect(component.filesToUpload()).toEqual([]);
      expect(component.pickedNodes).toEqual([]);
      expect(component.editPost()).toBeUndefined();
    });
  });

  describe('removeAttachment', () => {
    it('should remove attachment and add remaining size back', () => {
      component.attachmentRemainingSize.set({ remainingSize: 1000 });
      const attachment = { id: 'a1', size: 500, isLink: false };
      component.attachmentsAndLinksToEdit.set([attachment]);
      component.removeAttachment(attachment);
      expect(component.attachmentsAndLinksToEdit()).toEqual([]);
      expect(component.attachmentRemainingSize().remainingSize).toBe(1500);
    });

    it('should not add size back for link attachments', () => {
      component.attachmentRemainingSize.set({ remainingSize: 1000 });
      const attachment = { id: 'a2', isLink: true, size: 500 };
      component.attachmentsAndLinksToEdit.set([attachment]);
      component.removeAttachment(attachment);
      expect(component.attachmentRemainingSize().remainingSize).toBe(1000);
    });
  });

  describe('deleteSelectedFile', () => {
    it('should remove file and restore remaining size', () => {
      const file = new File(['content'], 'test.txt');
      Object.defineProperty(file, 'size', { value: 200 });
      component.attachmentRemainingSize.set({ remainingSize: 800 });
      component.filesToUpload.set([{ id: 0, file }]);
      component.deleteSelectedFile({ id: 0, file });
      expect(component.filesToUpload()).toEqual([]);
      expect(component.attachmentRemainingSize().remainingSize).toBe(1000);
    });
  });

  describe('openLinkPicker / closeLinkPicker', () => {
    it('should open link picker', () => {
      component.openLinkPicker();
      expect(component.linkPickerOpen()).toBe(true);
      expect(component.loadingPicker).toBe(true);
    });

    it('should close link picker and clear picked nodes', () => {
      component.pickedNodes = ['n1'];
      component.closeLinkPicker();
      expect(component.linkPickerOpen()).toBe(false);
      expect(component.pickedNodes).toEqual([]);
    });
  });

  describe('refreshComments', () => {
    beforeEach(() => {
      component.topicNode.set(mockTopicNode);
      component.group.set(mockGroup);
      fixture.detectChanges();
      dataSubject.next({ group: mockGroup });
    });

    it('should reload on successful post creation', async () => {
      mockNodesService.getNodeAsync.mockClear();
      await component.refreshComments({
        result: ActionResult.SUCCEED,
        type: ActionType.CREATE_POST,
      });
      expect(mockTopicService.getRepliesAsync).toHaveBeenCalled();
    });

    it('should show success message on delete', async () => {
      await component.refreshComments({
        result: ActionResult.SUCCEED,
        type: ActionType.DELETE_POST,
      });
      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    });
  });
});
