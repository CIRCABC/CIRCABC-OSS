import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { KeywordsService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeleteKeywordComponent } from './delete-keyword.component';

describe('DeleteKeywordComponent', () => {
  let component: DeleteKeywordComponent;
  let componentRef: ComponentRef<DeleteKeywordComponent>;
  let fixture: ComponentFixture<DeleteKeywordComponent>;

  const mockKeywordsService = {
    deleteKeywordDefinition: vi.fn().mockReturnValue(of(undefined)),
    deleteKeywordDefinitionAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteKeywordComponent],
      providers: [
        { provide: KeywordsService, useValue: mockKeywordsService },
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

    fixture = TestBed.createComponent(DeleteKeywordComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('keyword', { id: 'kw1', title: { en: 'Test' } });
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('delete', () => {
    it('should delete keyword and emit SUCCEED result', async () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.delete();

      expect(
        mockKeywordsService.deleteKeywordDefinitionAsync
      ).toHaveBeenCalledWith({ keywordId: 'kw1' });
      expect(component.showModal()).toBe(false);
      expect(component.deleting()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.SUCCEED,
          type: ActionType.DELETE_KEYWORD,
        })
      );
    });

    it('should not call service if keyword has no id', async () => {
      componentRef.setInput('keyword', { title: { en: 'No ID' } });
      fixture.detectChanges();

      await component.delete();

      expect(
        mockKeywordsService.deleteKeywordDefinitionAsync
      ).not.toHaveBeenCalled();
    });

    it('should emit FAILED result and show error message on locked error', async () => {
      mockKeywordsService.deleteKeywordDefinitionAsync.mockRejectedValue(
        new Error('locked')
      );
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.delete();

      expect(component.deleting()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.FAILED,
          type: ActionType.DELETE_KEYWORD,
        })
      );
    });
  });

  describe('cancelWizard', () => {
    it('should emit CANCELED result and hide modal', () => {
      component.showModal.set(true);
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('cancel');

      expect(component.showModal()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          result: ActionResult.CANCELED,
          type: ActionType.DELETE_KEYWORD,
        })
      );
    });
  });
});
