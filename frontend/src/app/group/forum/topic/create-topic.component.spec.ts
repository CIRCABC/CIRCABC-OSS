import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import {
  ContentService,
  ForumService,
  Node as ModelNode,
} from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CreateTopicComponent } from './create-topic.component';

describe('CreateTopicComponent', () => {
  let component: CreateTopicComponent;
  let componentRef: ComponentRef<CreateTopicComponent>;
  let fixture: ComponentFixture<CreateTopicComponent>;

  const mockForumService = {
    postForumContent: vi.fn(),
    postForumContentAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockContentService = {
    postTopic: vi.fn(),
    postTopicAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockI18nPipe = {
    transform: vi.fn().mockReturnValue('Test Title'),
  };

  const forumNode: ModelNode = { id: 'forum-id', type: 'forum' };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [CreateTopicComponent, ReactiveFormsModule],
      providers: [
        { provide: ForumService, useValue: mockForumService },
        { provide: ContentService, useValue: mockContentService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateTopicComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('forum', forumNode);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form on ngOnInit', () => {
    expect(component.newTopicForm).toBeDefined();
    expect(component.newTopicForm.controls['title']).toBeDefined();
    expect(component.newTopicForm.controls['name']).toBeDefined();
    expect(component.newTopicForm.controls['description']).toBeDefined();
  });

  it('should have title as required', () => {
    component.newTopicForm.controls['title'].setValue('');
    expect(component.newTopicForm.controls['title'].valid).toBe(false);
  });

  it('should return titleControl', () => {
    expect(component.titleControl).toBe(
      component.newTopicForm.controls['title']
    );
  });

  describe('cancelWizard', () => {
    it('should emit canceled result and reset form', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.newTopicForm.controls['title'].setValue('something');

      component.cancelWizard('close');

      expect(component.showWizard()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: ActionResult.CANCELED })
      );
      expect(component.newTopicForm.controls['title'].pristine).toBe(true);
    });
  });

  describe('createTopic', () => {
    it('should create topic via forumService when forum type includes "forum"', async () => {
      const responseNodes: ModelNode[] = [{ id: 'new-id', name: 'Test Title' }];
      mockForumService.postForumContentAsync.mockResolvedValue(responseNodes);
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.newTopicForm.controls['title'].setValue({ en: 'Test Title' });

      await component.createTopic();

      expect(mockForumService.postForumContentAsync).toHaveBeenCalledWith({
        id: 'forum-id',
        node: expect.objectContaining({ name: 'Test Title' }),
      });
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: ActionResult.SUCCEED })
      );
      expect(component.creating()).toBe(false);
      expect(component.showWizard()).toBe(false);
    });

    it('should create topic via contentService when forum type includes "content"', async () => {
      componentRef.setInput('forum', {
        id: 'content-id',
        type: 'content',
      } as ModelNode);
      fixture.detectChanges();

      const responseNode: ModelNode = { id: 'new-id', name: 'Test Title' };
      mockContentService.postTopicAsync.mockResolvedValue(responseNode);
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.newTopicForm.controls['title'].setValue({ en: 'Test Title' });

      await component.createTopic();

      expect(mockContentService.postTopicAsync).toHaveBeenCalledWith({
        id: 'content-id',
        node: expect.objectContaining({ name: 'Test Title' }),
      });
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: ActionResult.SUCCEED })
      );
      expect(component.creating()).toBe(false);
    });

    it('should handle error during creation', async () => {
      mockForumService.postForumContentAsync.mockRejectedValue(
        new Error('fail')
      );

      component.newTopicForm.controls['title'].setValue({ en: 'Test' });

      await component.createTopic();

      expect(component.creating()).toBe(false);
    });

    it('should not create topic when forum has no id', async () => {
      componentRef.setInput('forum', { type: 'forum' } as ModelNode);
      fixture.detectChanges();

      await component.createTopic();

      expect(mockForumService.postForumContentAsync).not.toHaveBeenCalled();
      expect(mockContentService.postTopicAsync).not.toHaveBeenCalled();
    });
  });
});
