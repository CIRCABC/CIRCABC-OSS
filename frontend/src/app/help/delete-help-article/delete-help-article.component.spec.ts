import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { HelpService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { DeleteHelpArticleComponent } from './delete-help-article.component';

describe('DeleteHelpArticleComponent', () => {
  let component: DeleteHelpArticleComponent;
  let fixture: ComponentFixture<DeleteHelpArticleComponent>;
  const mockHelpService = {
    deleteHelpArticle: vi.fn().mockReturnValue(of(undefined)),
    deleteHelpArticleAsync: vi.fn().mockResolvedValue(undefined),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteHelpArticleComponent],
      providers: [
        { provide: HelpService, useValue: mockHelpService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DeleteHelpArticleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('cancel', () => {
    it('should reset state and emit showModalChange', () => {
      const showModalChangeSpy = vi.spyOn(component.showModalChange, 'emit');
      component.showModal.set(true);
      component.articleId.set('123');

      component.cancel();

      expect(component.articleId()).toBeUndefined();
      expect(component.showModal()).toBe(false);
      expect(showModalChangeSpy).toHaveBeenCalledWith(false);
    });
  });

  describe('delete', () => {
    it('should delete article and emit success result', async () => {
      const articleDeletedSpy = vi.spyOn(component.articleDeleted, 'emit');
      component.articleId.set('abc');

      await component.delete();

      expect(mockHelpService.deleteHelpArticleAsync).toHaveBeenCalledWith({
        id: 'abc',
      });
      expect(component.articleId()).toBeUndefined();
      expect(component.showModal()).toBe(false);
      expect(component.deleting()).toBe(false);
      expect(articleDeletedSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_HELP_ARTICLE,
        result: ActionResult.SUCCEED,
      });
    });

    it('should emit result without success when articleId is undefined', async () => {
      const articleDeletedSpy = vi.spyOn(component.articleDeleted, 'emit');
      component.articleId.set(undefined);

      await component.delete();

      expect(mockHelpService.deleteHelpArticleAsync).not.toHaveBeenCalled();
      expect(articleDeletedSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_HELP_ARTICLE,
      });
    });

    it('should handle error and still emit result', async () => {
      mockHelpService.deleteHelpArticleAsync.mockRejectedValue(
        new Error('fail')
      );
      const articleDeletedSpy = vi.spyOn(component.articleDeleted, 'emit');
      component.articleId.set('abc');

      await component.delete();

      expect(component.deleting()).toBe(false);
      expect(articleDeletedSpy).toHaveBeenCalledWith({
        type: ActionType.DELETE_HELP_ARTICLE,
      });
    });
  });
});
