import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { ContentService, Node as ModelNode } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { EnableMultilingualComponent } from './enable-multilingual.component';

describe('EnableMultilingualComponent', () => {
  let component: EnableMultilingualComponent;
  let componentRef: ComponentRef<EnableMultilingualComponent>;
  let fixture: ComponentFixture<EnableMultilingualComponent>;

  const mockContentService = {
    postMultilingualAspect: vi.fn(),
    postMultilingualAspectAsync: vi.fn(),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  const mockNode: ModelNode = { id: 'node-123', name: 'test-node' };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [EnableMultilingualComponent, ReactiveFormsModule],
      providers: [
        { provide: ContentService, useValue: mockContentService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EnableMultilingualComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('targetNode', mockNode);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with author and lang controls', () => {
    expect(component.enableMultilingualForm).toBeDefined();
    expect(component.enableMultilingualForm.get('author')).toBeTruthy();
    expect(component.enableMultilingualForm.get('lang')).toBeTruthy();
  });

  it('should set default lang to en', () => {
    expect(component.enableMultilingualForm.get('lang')?.value).toBe('en');
  });

  it('should require author field', () => {
    const authorControl = component.authorControl;
    authorControl.setValue('');
    expect(authorControl.valid).toBe(false);

    authorControl.setValue('John');
    expect(authorControl.valid).toBe(true);
  });

  describe('enableMultilingual', () => {
    beforeEach(() => {
      component.enableMultilingualForm.patchValue({
        author: 'Test Author',
        lang: 'fr',
      });
    });

    it('should call contentService and emit success result', async () => {
      mockContentService.postMultilingualAspectAsync.mockResolvedValue({});
      const emitSpy = vi.spyOn(component.mutlilingualEnabled, 'emit');

      await component.enableMultilingual();

      expect(
        mockContentService.postMultilingualAspectAsync
      ).toHaveBeenCalledWith({
        id: 'node-123',
        multilingualAspectMetadata: { pivotLang: 'fr', author: 'Test Author' },
      });
      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.ENABLE_MULTILINGUAL,
        result: ActionResult.SUCCEED,
      });
      expect(component.processing()).toBe(false);
    });

    it('should emit failed result and show error on failure', async () => {
      mockContentService.postMultilingualAspectAsync.mockRejectedValue(
        new Error('fail')
      );
      const emitSpy = vi.spyOn(component.mutlilingualEnabled, 'emit');

      await component.enableMultilingual();

      expect(emitSpy).toHaveBeenCalledWith({
        type: ActionType.ENABLE_MULTILINGUAL,
        result: ActionResult.FAILED,
      });
      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
      expect(component.processing()).toBe(false);
    });

    it('should not call service if targetNode has no id', async () => {
      componentRef.setInput('targetNode', { name: 'no-id' } as ModelNode);
      fixture.detectChanges();

      await component.enableMultilingual();

      expect(
        mockContentService.postMultilingualAspectAsync
      ).not.toHaveBeenCalled();
    });
  });

  describe('cancel', () => {
    it('should emit canceled result', () => {
      const emitSpy = vi.spyOn(component.modalCanceled, 'emit');

      component.cancel();

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.ENABLE_MULTILINGUAL,
      });
      expect(component.processing()).toBe(false);
    });
  });
});
