import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { KeywordDefinition, KeywordsService } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeleteMultiKeywordsComponent } from './delete-multi-keywords.component';

function makeKeyword(id: string): KeywordDefinition {
  return { id, title: { en: 'test' } };
}

const mockKeywordsService = {
  deleteKeywordDefinition: vi.fn().mockReturnValue(of(undefined)),
  deleteKeywordDefinitionAsync: vi.fn().mockResolvedValue(undefined),
};

const mockTranslocoService = {
  translate: vi.fn().mockReturnValue('error text'),
};

const mockUiMessageService = {
  addErrorMessage: vi.fn(),
};

describe('DeleteMultiKeywordsComponent', () => {
  let component: DeleteMultiKeywordsComponent;
  let fixture: ComponentFixture<DeleteMultiKeywordsComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [DeleteMultiKeywordsComponent],
      providers: [
        { provide: KeywordsService, useValue: mockKeywordsService },
        { provide: TranslocoService, useValue: mockTranslocoService },
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

    fixture = TestBed.createComponent(DeleteMultiKeywordsComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('keywords', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('ngOnChanges', () => {
    it('should set progressMax from keywords length', () => {
      const keywords = [makeKeyword('1'), makeKeyword('2')];
      component.ngOnChanges({
        keywords: {
          currentValue: keywords,
          previousValue: [],
          firstChange: true,
          isFirstChange: () => true,
        },
      });
      expect(component.progressMax).toBe(2);
      expect(component.progressValue()).toBe(0);
      expect(component.deleting()).toBe(false);
    });

    it('should set progressMax to 0 when currentValue is null', () => {
      component.ngOnChanges({
        keywords: {
          currentValue: null,
          previousValue: [],
          firstChange: false,
          isFirstChange: () => false,
        },
      });
      expect(component.progressMax).toBe(0);
    });
  });

  describe('deleteAll', () => {
    it('should delete all keywords and emit SUCCEED result', async () => {
      const keywords = [makeKeyword('1'), makeKeyword('2')];
      fixture.componentRef.setInput('keywords', keywords);
      fixture.detectChanges();

      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      await component.deleteAll();

      expect(
        mockKeywordsService.deleteKeywordDefinitionAsync
      ).toHaveBeenCalledWith({ keywordId: '1' });
      expect(
        mockKeywordsService.deleteKeywordDefinitionAsync
      ).toHaveBeenCalledWith({ keywordId: '2' });
      expect(component.progressValue()).toBe(2);
      expect(component.deleting()).toBe(false);
      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.SUCCEED,
        type: ActionType.DELETE_ALL,
      });
    });

    it('should skip keywords without id', async () => {
      fixture.componentRef.setInput('keywords', [
        { title: { en: 'x' } } as KeywordDefinition,
      ]);
      fixture.detectChanges();

      await component.deleteAll();

      expect(
        mockKeywordsService.deleteKeywordDefinitionAsync
      ).not.toHaveBeenCalled();
    });

    it('should show error message when deletion fails with locked', async () => {
      mockKeywordsService.deleteKeywordDefinitionAsync.mockRejectedValueOnce({
        message: 'resource is locked',
      });
      fixture.componentRef.setInput('keywords', [makeKeyword('1')]);
      fixture.detectChanges();

      await component.deleteAll();

      expect(mockTranslocoService.translate).toHaveBeenCalledWith(
        'keywords.deletion.failed.locked.document'
      );
      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(
        'error text',
        false
      );
    });
  });

  describe('cancelWizard', () => {
    it('should emit CANCELED result', () => {
      const emitSpy = vi.spyOn(component.modalHide, 'emit');

      component.cancelWizard('cancel');

      expect(emitSpy).toHaveBeenCalledWith({
        result: ActionResult.CANCELED,
        type: ActionType.DELETE_ALL,
      });
    });
  });
});
