import {
  ComponentRef,
  NO_ERRORS_SCHEMA,
  SimpleChange,
  SimpleChanges,
} from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import {
  Node as ModelNode,
  PostService,
  TopicService,
  UserService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddPostComponent } from './add-post.component';

describe('AddPostComponent', () => {
  let component: AddPostComponent;
  let componentRef: ComponentRef<AddPostComponent>;
  let fixture: ComponentFixture<AddPostComponent>;

  const mockTopicService = {
    postReply: vi.fn().mockReturnValue(of({})),
    postReplyAsync: vi.fn().mockResolvedValue({}),
  };

  const mockPostService = {
    putPost: vi.fn().mockReturnValue(of({})),
    putPostAsync: vi.fn().mockResolvedValue({}),
  };

  const mockUserService = {
    getUserAsync: vi
      .fn()
      .mockResolvedValue({ firstname: 'John', lastname: 'Doe' }),
  };

  const mockTopic: ModelNode = { id: 'topic-1', name: 'Test Topic' };

  beforeEach(async () => {
    vi.clearAllMocks();

    TestBed.configureTestingModule({
      imports: [AddPostComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: TopicService, useValue: mockTopicService },
        { provide: PostService, useValue: mockPostService },
        { provide: UserService, useValue: mockUserService },
      ],
    }).overrideComponent(AddPostComponent, {
      set: {
        template: '',
        imports: [ReactiveFormsModule, TranslocoModule],
        schemas: [NO_ERRORS_SCHEMA],
      },
    });

    fixture = TestBed.createComponent(AddPostComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('topic', mockTopic);
    componentRef.setInput('futureQuote', undefined);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize the form with empty text and required validator', () => {
    expect(component.addPostForm).toBeDefined();
    expect(component.addPostForm.get('text')?.value).toBe('');
    expect(component.addPostForm.valid).toBe(false);
  });

  describe('resetForm', () => {
    it('should hide form and reset form values', () => {
      component.showForm.set(true);
      component.addPostForm.patchValue({ text: 'some text' });

      component.resetForm();

      expect(component.showForm()).toBe(false);
      expect(component.addPostForm.get('text')?.value).toBeNull();
    });
  });

  describe('ngOnChanges', () => {
    it('should patch form with quote text when futureQuote changes', async () => {
      const changes: SimpleChanges = {
        futureQuote: new SimpleChange(
          undefined,
          { author: 'user1', text: 'Hello' },
          false
        ),
      };

      component.ngOnChanges(changes);
      await new Promise((resolve) => setTimeout(resolve));

      expect(mockUserService.getUserAsync).toHaveBeenCalledWith({
        userId: 'user1',
      });
      expect(component.addPostForm.get('text')?.value).toContain('Hello');
      expect(component.showForm()).toBe(true);
    });

    it('should patch form with edit post message when editPost changes', async () => {
      const editNode: ModelNode = {
        id: 'post-1',
        properties: { message: 'Edit me' },
      };
      const changes: SimpleChanges = {
        editPost: new SimpleChange(undefined, editNode, false),
      };

      component.ngOnChanges(changes);
      await new Promise((resolve) => setTimeout(resolve));

      expect(component.addPostForm.get('text')?.value).toBe('Edit me');
      expect(component.showForm()).toBe(true);
    });

    it('should reset form when topic changes', async () => {
      component.showForm.set(true);
      component.addPostForm.patchValue({ text: 'something' });

      const changes: SimpleChanges = {
        topic: new SimpleChange(undefined, mockTopic, false),
      };

      component.ngOnChanges(changes);
      await new Promise((resolve) => setTimeout(resolve));

      expect(component.showForm()).toBe(false);
    });
  });

  describe('addPost', () => {
    it('should post reply and emit success result', async () => {
      component.addPostForm.patchValue({ text: 'New post content' });
      const emitSpy = vi.spyOn(component.postedComment, 'emit');

      await component.addPost();

      expect(mockTopicService.postReplyAsync).toHaveBeenCalledWith({
        id: 'topic-1',
        notify: true,
        comment: {
          text: 'New post content',
        },
      });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.CREATE_POST,
        result: ActionResult.SUCCEED,
      });
      expect(component.showForm()).toBe(false);
      expect(component.posting()).toBe(false);
    });

    it('should not post if form is invalid', async () => {
      const emitSpy = vi.spyOn(component.postedComment, 'emit');

      await component.addPost();

      expect(mockTopicService.postReplyAsync).not.toHaveBeenCalled();
      expect(emitSpy).not.toHaveBeenCalled();
    });

    it('should emit failed result on error', async () => {
      component.addPostForm.patchValue({ text: 'New post content' });
      mockTopicService.postReplyAsync.mockRejectedValueOnce(new Error('fail'));
      const emitSpy = vi.spyOn(component.postedComment, 'emit');

      await component.addPost();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.CREATE_POST,
        result: ActionResult.FAILED,
      });
      expect(component.posting()).toBe(false);
    });
  });

  describe('updatePost', () => {
    it('should update post and emit success result', async () => {
      const editNode: ModelNode = {
        id: 'post-1',
        properties: { message: 'Old' },
      };
      componentRef.setInput('editPost', editNode);
      fixture.detectChanges();

      component.addPostForm.patchValue({ text: 'Updated content' });
      const emitSpy = vi.spyOn(component.postedComment, 'emit');

      await component.updatePost();

      expect(mockPostService.putPostAsync).toHaveBeenCalledWith({
        id: 'post-1',
        notify: false,
        post: editNode,
      });
      expect(editNode.properties?.['message']).toBe('Updated content');
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.EDIT_POST,
        result: ActionResult.SUCCEED,
      });
      expect(component.showForm()).toBe(false);
      expect(component.posting()).toBe(false);
    });

    it('should not update if form is invalid', async () => {
      componentRef.setInput('editPost', {
        id: 'post-1',
        properties: { message: 'Old' },
      });
      fixture.detectChanges();
      component.addPostForm.patchValue({ text: '' });
      const emitSpy = vi.spyOn(component.postedComment, 'emit');

      await component.updatePost();

      expect(mockPostService.putPostAsync).not.toHaveBeenCalled();
      expect(emitSpy).not.toHaveBeenCalled();
    });

    it('should emit failed result on error', async () => {
      const editNode: ModelNode = {
        id: 'post-1',
        properties: { message: 'Old' },
      };
      componentRef.setInput('editPost', editNode);
      fixture.detectChanges();

      component.addPostForm.patchValue({ text: 'Updated content' });
      mockPostService.putPostAsync.mockRejectedValueOnce(new Error('fail'));
      const emitSpy = vi.spyOn(component.postedComment, 'emit');

      await component.updatePost();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.EDIT_POST,
        result: ActionResult.FAILED,
      });
      expect(component.posting()).toBe(false);
    });
  });
});
