import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result/action-result';
import { ActionType } from 'app/action-result/action-type';
import { ContentService, Node as ModelNode } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CreateDetailsTopicComponent } from './create-details-topic.component';

describe('CreateDetailsTopicComponent', () => {
  let component: CreateDetailsTopicComponent;
  let componentRef: ComponentRef<CreateDetailsTopicComponent>;
  let fixture: ComponentFixture<CreateDetailsTopicComponent>;

  const mockContentService = {
    postTopic: vi.fn(),
    postTopicAsync: vi.fn(),
  };

  const mockForum: ModelNode = { id: 'forum-1', name: 'Test Forum' };

  beforeEach(async () => {
    mockContentService.postTopic.mockReset();
    mockContentService.postTopicAsync.mockReset();

    await TestBed.configureTestingModule({
      imports: [CreateDetailsTopicComponent, ReactiveFormsModule],
      providers: [
        { provide: ContentService, useValue: mockContentService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateDetailsTopicComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('forum', mockForum);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should initialize form with empty name', () => {
    expect(component.createTopicForm).toBeDefined();
    expect(component.nameControl.value).toBe('');
  });

  it('should mark form invalid when name is empty', () => {
    expect(component.createTopicForm.valid).toBe(false);
  });

  it('should mark form valid with a proper name', () => {
    component.nameControl.setValue('Valid Topic');
    expect(component.createTopicForm.valid).toBe(true);
  });

  it('should invalidate name exceeding 50 characters', () => {
    component.nameControl.setValue('a'.repeat(51));
    expect(component.nameControl.hasError('maxlength')).toBe(true);
  });

  describe('cancel', () => {
    it('should emit CANCELED result and reset form', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.nameControl.setValue('Something');

      component.cancel();

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.CREATE_TOPIC,
      });
      expect(component.nameControl.value).toBe('');
    });
  });

  describe('save', () => {
    it('should call postTopic and emit SUCCEED on success', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      mockContentService.postTopicAsync.mockResolvedValue({
        id: 'new-topic',
      } as ModelNode);
      component.nameControl.setValue('New Topic');

      await component.save();

      expect(mockContentService.postTopicAsync).toHaveBeenCalledWith({
        id: 'forum-1',
        node: {
          name: 'New Topic',
        },
      });
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.CREATE_TOPIC,
      });
      expect(component.creating()).toBe(false);
    });

    it('should emit FAILED on error', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      mockContentService.postTopicAsync.mockRejectedValue(new Error('fail'));
      component.nameControl.setValue('New Topic');

      await component.save();

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.FAILED,
        type: ActionType.CREATE_TOPIC,
      });
      expect(component.creating()).toBe(false);
    });
  });
});
