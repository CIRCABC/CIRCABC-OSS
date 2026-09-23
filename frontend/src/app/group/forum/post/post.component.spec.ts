import { DatePipe } from '@angular/common';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { PermissionEvaluatorService } from 'app/core/evaluator/permission-evaluator.service';
import {
  BASE_PATH,
  Node as ModelNode,
  NodesService,
  PostService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { SaveAsService } from 'app/core/save-as.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { PostComponent } from './post.component';

@Pipe({ name: 'cbcDownload' })
class MockDownloadPipe implements PipeTransform {
  transform(value: unknown): unknown {
    return value;
  }
}

@Pipe({ name: 'cbcIfRoleGE' })
class MockIfRoleGePipe implements PipeTransform {
  transform(value: unknown): unknown {
    return value;
  }
}

@Pipe({ name: 'cbcSecure' })
class MockSecurePipe implements PipeTransform {
  transform(value: unknown): unknown {
    return value;
  }
}

const mockPostService = {
  deletePost: vi.fn().mockReturnValue(of(undefined)),
  deletePostAsync: vi.fn().mockResolvedValue(undefined),
  putVerify: vi.fn().mockReturnValue(of(undefined)),
  putVerifyAsync: vi.fn().mockResolvedValue(undefined),
  deleteAbuse: vi.fn().mockReturnValue(of(undefined)),
  deleteAbuseAsync: vi.fn().mockResolvedValue(undefined),
  getAbusesAsync: vi.fn().mockResolvedValue([]),
};

const mockNodesService = {
  getNodeAsync: vi.fn().mockResolvedValue({ type: 'cm:folder' }),
};

const mockPermEvalService = {
  isLibAdmin: vi.fn().mockReturnValue(false),
  isNewsgroupAdmin: vi.fn().mockReturnValue(false),
  isNewsgroupModerate: vi.fn().mockReturnValue(false),
  isNewsgroupPost: vi.fn().mockReturnValue(false),
  isLibAccess: vi.fn().mockReturnValue(false),
  isLibAdminOrFullEdit: vi.fn().mockReturnValue(false),
};

const mockLoginService = {
  getCurrentUsername: vi.fn().mockReturnValue('testuser'),
};

const mockSaveAsService = {
  saveUrlAs: vi.fn(),
};

const mockRouter = {
  navigate: vi.fn(),
};

function createPost(overrides: Partial<ModelNode> = {}): ModelNode {
  return {
    id: 'post-1',
    properties: {
      creator: 'author',
      owner: 'testuser',
      message: '<p>Hello world</p>',
      versionLabel: '1.0',
      waitingForApproval: 'false',
      created: '2024-01-01',
      modified: '2024-01-02',
      modifier: 'editor',
      avatar: 'avatar-url',
    },
    permissions: {},
    ...overrides,
  };
}

describe('PostComponent', () => {
  let fixture: ComponentFixture<PostComponent>;
  let component: PostComponent;

  beforeEach(async () => {
    vi.clearAllMocks();
    mockLoginService.getCurrentUsername.mockReturnValue('testuser');
    mockPermEvalService.isLibAdmin.mockReturnValue(false);
    mockPermEvalService.isNewsgroupAdmin.mockReturnValue(false);
    mockPermEvalService.isNewsgroupModerate.mockReturnValue(false);
    mockPermEvalService.isNewsgroupPost.mockReturnValue(false);
    mockPermEvalService.isLibAccess.mockReturnValue(false);
    mockPermEvalService.isLibAdminOrFullEdit.mockReturnValue(false);

    await TestBed.configureTestingModule({
      imports: [PostComponent],
      providers: [
        { provide: PostService, useValue: mockPostService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: PermissionEvaluatorService, useValue: mockPermEvalService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: SaveAsService, useValue: mockSaveAsService },
        { provide: Router, useValue: mockRouter },
        { provide: BASE_PATH, useValue: 'http://localhost' },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    })
      .overrideComponent(PostComponent, {
        set: {
          imports: [
            DatePipe,
            TranslocoModule,
            MockDownloadPipe,
            MockIfRoleGePipe,
            MockSecurePipe,
          ],
          schemas: [NO_ERRORS_SCHEMA],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(PostComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('post', createPost());
    fixture.componentRef.setInput('topic', {
      permissions: { NwsModerate: 'ALLOWED' },
    });
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  describe('fireReply', () => {
    it('should emit replyClicked with quote', () => {
      fixture.detectChanges();
      const emitSpy = vi.spyOn(component.replyClicked, 'emit');

      component.fireReply();

      expect(emitSpy).toHaveBeenCalledWith({
        author: 'author',
        text: '<p>Hello world</p>',
      });
    });

    it('should strip trailing quotes from message', () => {
      fixture.componentRef.setInput(
        'post',
        createPost({
          properties: {
            creator: 'author',
            owner: 'testuser',
            message:
              '<p>Real content</p><p><br></p><p>someone: first last</p><blockquote>quoted</blockquote>',
            versionLabel: '1.0',
            waitingForApproval: 'false',
            created: '2024-01-01',
            modified: '2024-01-02',
            modifier: 'editor',
            avatar: '',
          },
        })
      );
      fixture.detectChanges();
      const emitSpy = vi.spyOn(component.replyClicked, 'emit');

      component.fireReply();

      expect(emitSpy).toHaveBeenCalledWith({
        author: 'author',
        text: '<p>Real content</p>',
      });
    });
  });

  describe('fireEdit', () => {
    it('should emit editClicked with the post', () => {
      fixture.detectChanges();
      const emitSpy = vi.spyOn(component.editClicked, 'emit');

      component.fireEdit();

      expect(emitSpy).toHaveBeenCalledWith(createPost());
    });
  });

  describe('deletePost', () => {
    it('should call postService.deletePost and emit success', async () => {
      fixture.detectChanges();
      const emitSpy = vi.spyOn(component.deleted, 'emit');

      await component.deletePost();

      expect(mockPostService.deletePostAsync).toHaveBeenCalledWith({
        id: 'post-1',
      });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_POST,
        result: ActionResult.SUCCEED,
      });
    });

    it('should emit failed result on error', async () => {
      mockPostService.deletePostAsync.mockRejectedValueOnce(new Error('fail'));
      fixture.detectChanges();
      const emitSpy = vi.spyOn(component.deleted, 'emit');

      await component.deletePost();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_POST,
        result: ActionResult.FAILED,
      });
    });
  });

  describe('canDeleteComment', () => {
    it('should return true if user is owner', () => {
      fixture.detectChanges();
      expect(component.canDeleteComment()).toBe(true);
    });

    it('should return true if user is lib admin', () => {
      mockLoginService.getCurrentUsername.mockReturnValue('other');
      fixture.componentRef.setInput(
        'post',
        createPost({
          properties: {
            owner: 'someone',
            creator: 'someone',
            message: '',
            versionLabel: '1.0',
            waitingForApproval: 'false',
            created: '',
            modified: '',
            modifier: '',
            avatar: '',
          },
        })
      );
      mockPermEvalService.isLibAdmin.mockReturnValue(true);
      fixture.detectChanges();

      expect(component.canDeleteComment()).toBe(true);
    });

    it('should return false if no permissions', () => {
      mockLoginService.getCurrentUsername.mockReturnValue('other');
      fixture.componentRef.setInput(
        'post',
        createPost({
          properties: {
            owner: 'someone',
            creator: 'someone',
            message: '',
            versionLabel: '1.0',
            waitingForApproval: 'false',
            created: '',
            modified: '',
            modifier: '',
            avatar: '',
          },
        })
      );
      fixture.detectChanges();

      expect(component.canDeleteComment()).toBe(false);
    });
  });

  describe('canPostComment', () => {
    it('should return false by default', () => {
      fixture.detectChanges();
      expect(component.canPostComment()).toBe(false);
    });

    it('should return true if newsgroup post allowed', () => {
      mockPermEvalService.isNewsgroupPost.mockReturnValue(true);
      fixture.detectChanges();

      expect(component.canPostComment()).toBe(true);
    });
  });

  describe('isOwner', () => {
    it('should return true when current user matches owner', () => {
      fixture.detectChanges();
      expect(component.isOwner()).toBe(true);
    });

    it('should return false when current user does not match owner', () => {
      mockLoginService.getCurrentUsername.mockReturnValue('other');
      fixture.detectChanges();

      expect(component.isOwner()).toBe(false);
    });
  });

  describe('approve', () => {
    it('should call putVerify and emit verified result', async () => {
      fixture.detectChanges();
      const emitSpy = vi.spyOn(component.verified, 'emit');

      await component.approve();

      expect(mockPostService.putVerifyAsync).toHaveBeenCalledWith({
        id: 'post-1',
        approve: true,
      });
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.APPROVE_POST,
      });
    });
  });

  describe('waitingForApproval', () => {
    it('should return false when not waiting', () => {
      fixture.detectChanges();
      expect(component.waitingForApproval()).toBe(false);
    });

    it('should return true when waiting', () => {
      fixture.componentRef.setInput(
        'post',
        createPost({
          properties: {
            creator: 'a',
            owner: 'a',
            message: '',
            versionLabel: '1.0',
            waitingForApproval: 'true',
            created: '',
            modified: '',
            modifier: '',
            avatar: '',
          },
        })
      );
      fixture.detectChanges();

      expect(component.waitingForApproval()).toBe(true);
    });
  });

  describe('isModerator', () => {
    it('should return false when no permissions on topic', () => {
      fixture.componentRef.setInput('topic', { permissions: {} });
      fixture.detectChanges();

      expect(component.isModerator()).toBe(false);
    });

    it('should return true when NwsModerate is ALLOWED', () => {
      fixture.detectChanges();
      expect(component.isModerator()).toBe(true);
    });
  });

  describe('hasBeenEdited', () => {
    it('should return false for version 1.0', () => {
      fixture.detectChanges();
      expect(component.hasBeenEdited()).toBe(false);
    });

    it('should return true for version other than 1.0', () => {
      fixture.componentRef.setInput(
        'post',
        createPost({
          properties: {
            creator: 'a',
            owner: 'a',
            message: '',
            versionLabel: '2.0',
            waitingForApproval: 'false',
            created: '',
            modified: '',
            modifier: '',
            avatar: '',
          },
        })
      );
      fixture.detectChanges();

      expect(component.hasBeenEdited()).toBe(true);
    });
  });

  describe('hasBeenRejected', () => {
    it('should return false when no rejectedOn', () => {
      fixture.detectChanges();
      expect(component.hasBeenRejected()).toBe(false);
    });

    it('should return true when rejectedOn is set', () => {
      fixture.componentRef.setInput(
        'post',
        createPost({
          properties: {
            creator: 'a',
            owner: 'a',
            message: '',
            versionLabel: '1.0',
            waitingForApproval: 'false',
            created: '',
            modified: '',
            modifier: '',
            avatar: '',
            rejectedOn: '2024-01-01',
          },
        })
      );
      fixture.detectChanges();

      expect(component.hasBeenRejected()).toBe(true);
    });
  });

  describe('abuseSignaled', () => {
    it('should return false when no abuse messages', () => {
      fixture.detectChanges();
      expect(component.abuseSignaled()).toBeFalsy();
    });

    it('should return true when messages contain Abuse Report', () => {
      fixture.componentRef.setInput(
        'post',
        createPost({
          properties: {
            creator: 'a',
            owner: 'a',
            message: '',
            versionLabel: '1.0',
            waitingForApproval: 'false',
            created: '',
            modified: '',
            modifier: '',
            avatar: '',
            messages: 'Abuse Report: something',
          },
        })
      );
      fixture.detectChanges();

      expect(component.abuseSignaled()).toBe(true);
    });
  });

  describe('removeAbuses', () => {
    it('should call deleteAbuse and emit verified', async () => {
      fixture.detectChanges();
      const emitSpy = vi.spyOn(component.verified, 'emit');

      await component.removeAbuses();

      expect(mockPostService.deleteAbuseAsync).toHaveBeenCalledWith({
        id: 'post-1',
      });
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.DELETE_ABUSE,
      });
    });
  });

  describe('toggleShowAbuses', () => {
    it('should fetch abuses and toggle visibility', async () => {
      fixture.detectChanges();
      expect(component.showSignaledAbuses()).toBe(false);

      await component.toggleShowAbuses();

      expect(mockPostService.getAbusesAsync).toHaveBeenCalledWith({
        id: 'post-1',
      });
      expect(component.showSignaledAbuses()).toBe(true);
    });

    it('should toggle off without fetching', async () => {
      fixture.detectChanges();
      component.showSignaledAbuses.set(true);

      await component.toggleShowAbuses();

      expect(component.showSignaledAbuses()).toBe(false);
    });
  });

  describe('displayName', () => {
    it('should return empty string for undefined', () => {
      expect(component.displayName(undefined)).toBe('');
    });

    it('should return full text if 7 chars or less', () => {
      expect(component.displayName('short')).toBe('short');
    });

    it('should truncate text longer than 7 chars', () => {
      expect(component.displayName('longfilename.pdf')).toBe('longfil...');
    });
  });

  describe('accessAttachment', () => {
    it('should navigate for link attachments ending in folder', async () => {
      fixture.componentRef.setInput('igId', 'ig-1');
      mockNodesService.getNodeAsync.mockResolvedValueOnce({
        type: 'cm:folder',
      });
      fixture.detectChanges();

      await component.accessAttachment({
        id: 'att-1',
        isLink: true,
        name: 'doc',
      });

      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'att-1',
      });
      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group/ig-1/library/att-1',
      ]);
    });

    it('should navigate with /details for non-folder link', async () => {
      fixture.componentRef.setInput('igId', 'ig-1');
      mockNodesService.getNodeAsync.mockResolvedValueOnce({
        type: 'cm:content',
      });
      fixture.detectChanges();

      await component.accessAttachment({
        id: 'att-1',
        isLink: true,
        name: 'doc',
      });

      expect(mockRouter.navigate).toHaveBeenCalledWith([
        '/group/ig-1/library/att-1/details',
      ]);
    });

    it('should download for non-link attachments', async () => {
      fixture.detectChanges();

      await component.accessAttachment({
        id: 'att-1',
        isLink: false,
        name: 'file.pdf',
      });

      expect(mockSaveAsService.saveUrlAs).toHaveBeenCalledWith(
        'http://localhost/posts/att-1/attachment/download',
        'file.pdf'
      );
    });
  });

  describe('getters', () => {
    it('should return avatar from post properties', () => {
      fixture.detectChanges();
      expect(component.avatar).toBe('avatar-url');
    });

    it('should return creator from post properties', () => {
      fixture.detectChanges();
      expect(component.creator).toBe('author');
    });

    it('should return null when no properties', () => {
      fixture.componentRef.setInput('post', { id: 'x' });
      fixture.detectChanges();
      expect(component.avatar).toBeNull();
    });
  });
});
