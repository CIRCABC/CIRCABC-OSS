import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import { ActionService } from 'app/action-result/action.service';
import { ForumService, Node as ModelNode } from 'app/core/generated/circabc';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CreateForumComponent } from './create-forum.component';

describe('CreateForumComponent', () => {
  let component: CreateForumComponent;
  let componentRef: ComponentRef<CreateForumComponent>;
  let fixture: ComponentFixture<CreateForumComponent>;

  const mockForumService = {
    postSubforums: vi.fn(),
    postSubforumsAsync: vi.fn(),
  };

  const mockActionService = {
    propagateActionFinished: vi.fn(),
  };

  const mockI18nPipe = {
    transform: vi.fn().mockReturnValue('Test Title'),
  };

  const parentNode: ModelNode = { id: 'parent-id', name: 'Parent' };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [CreateForumComponent, ReactiveFormsModule],
      providers: [
        { provide: ForumService, useValue: mockForumService },
        { provide: ActionService, useValue: mockActionService },
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

    fixture = TestBed.createComponent(CreateForumComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('parentNode', parentNode);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form on ngOnInit', () => {
    expect(component.createForumForm).toBeDefined();
    expect(component.createForumForm.controls['title']).toBeDefined();
    expect(component.createForumForm.controls['name']).toBeDefined();
    expect(component.createForumForm.controls['description']).toBeDefined();
  });

  it('should have title as required', () => {
    component.createForumForm.controls['title'].setValue('');
    expect(component.createForumForm.controls['title'].valid).toBe(false);
  });

  it('should return titleControl', () => {
    expect(component.titleControl).toBe(
      component.createForumForm.controls['title']
    );
  });

  describe('cancelWizard', () => {
    it('should emit canceled result and reset form', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');
      component.createForumForm.controls['title'].setValue('something');

      component.cancelWizard('close');

      expect(component.showWizard()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: ActionResult.CANCELED })
      );
      expect(mockActionService.propagateActionFinished).toHaveBeenCalled();
      expect(component.createForumForm.controls['title'].pristine).toBe(true);
    });
  });

  describe('createForum', () => {
    it('should create forum successfully', async () => {
      const responseNode: ModelNode = { id: 'new-id', name: 'Test Title' };
      mockForumService.postSubforumsAsync.mockResolvedValue(responseNode);
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.createForumForm.controls['title'].setValue({
        en: 'Test Title',
      });

      await component.createForum();

      expect(mockForumService.postSubforumsAsync).toHaveBeenCalledWith({
        id: 'parent-id',
        node: expect.objectContaining({ name: 'Test Title' }),
      });
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.SUCCEED,
          node: responseNode,
        })
      );
      expect(component.creating()).toBe(false);
    });

    it('should handle error during creation', async () => {
      mockForumService.postSubforumsAsync.mockRejectedValue(new Error('fail'));
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.createForumForm.controls['title'].setValue({ en: 'Test' });

      await component.createForum();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({ result: ActionResult.FAILED })
      );
      expect(component.creating()).toBe(false);
    });
  });
});
