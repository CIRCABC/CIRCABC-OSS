import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult, ActionType } from 'app/action-result';
import { HelpArticle, HelpService } from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { AddHelpArticleComponent } from './add-help-article.component';

const mockArticle: HelpArticle = {
  id: 'article-1',
  title: { en: 'Test Article' },
  content: { en: '<p>Test content</p>' },
};

const mockHelpService = {
  getHelpArticleAsync: vi.fn().mockResolvedValue(mockArticle),
  createCategoryArticle: vi.fn().mockReturnValue(of(mockArticle)),
  createCategoryArticleAsync: vi.fn().mockResolvedValue(mockArticle),
  updateHelpArticle: vi.fn().mockReturnValue(of(mockArticle)),
  updateHelpArticleAsync: vi.fn().mockResolvedValue(mockArticle),
};

describe('AddHelpArticleComponent', () => {
  let component: AddHelpArticleComponent;
  let fixture: ComponentFixture<AddHelpArticleComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [AddHelpArticleComponent, ReactiveFormsModule],
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

    fixture = TestBed.createComponent(AddHelpArticleComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeDefined();
  });

  it('should initialize form on ngOnInit', () => {
    fixture.detectChanges();
    expect(component.newArticlForm).toBeDefined();
    expect(component.newArticlForm.controls['title']).toBeDefined();
    expect(component.newArticlForm.controls['currentLang']).toBeDefined();
    expect(component.newArticlForm.controls['content']).toBeDefined();
  });

  it('should set availableLangs on init', () => {
    fixture.detectChanges();
    expect(component.availableLangs.length).toBeGreaterThan(0);
  });

  it('should compute validity as false when form is empty', () => {
    fixture.detectChanges();
    expect(component.isValid()).toBe(false);
  });

  it('should compute validity as true when title and content model are set', () => {
    fixture.detectChanges();
    component.newArticlForm.controls['title'].setValue({ en: 'A title' });
    component.model = { en: '<p>Some content</p>' };
    component['computeValidity']();
    expect(component.isValid()).toBe(true);
  });

  it('should switch lang and patch content from model', () => {
    fixture.detectChanges();
    component.model = { en: '<p>English</p>', fr: '<p>French</p>' };
    component.switchLang('fr');
    expect(component.newArticlForm.value.content).toBe('<p>French</p>');
  });

  it('should switch lang and set empty content if lang not in model', () => {
    fixture.detectChanges();
    component.model = { en: '<p>English</p>' };
    component.switchLang('de');
    expect(component.newArticlForm.value.content).toBe('');
  });

  it('should sync text into model', () => {
    fixture.detectChanges();
    component.newArticlForm.patchValue({ currentLang: 'en' });
    component.syncText({ htmlValue: '<p>Updated</p>' });
    expect(component.model['en']).toBe('<p>Updated</p>');
  });

  it('should cancel and reset modal', () => {
    fixture.detectChanges();
    component.showModal.set(true);
    component.cancel();
    expect(component.showModal()).toBe(false);
  });

  describe('createArticle', () => {
    it('should return early if categoryId is undefined', async () => {
      fixture.detectChanges();
      await component.createArticle();
      expect(mockHelpService.createCategoryArticleAsync).not.toHaveBeenCalled();
    });

    it('should create article and emit success', async () => {
      fixture.componentRef.setInput('categoryId', 'cat-1');
      fixture.detectChanges();

      component.newArticlForm.patchValue({ title: { en: 'New' } });
      component.model = { en: '<p>Content</p>' };

      const emitSpy = vi.spyOn(component.articleCreated, 'emit');
      await component.createArticle();

      expect(mockHelpService.createCategoryArticleAsync).toHaveBeenCalledWith({
        id: 'cat-1',
        helpArticle: expect.objectContaining({
          content: { en: '<p>Content</p>' },
        }),
      });
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.ADD_HELP_ARTICLE,
          result: ActionResult.SUCCEED,
        })
      );
      expect(component.creating()).toBe(false);
    });

    it('should emit failed result on error', async () => {
      fixture.componentRef.setInput('categoryId', 'cat-1');
      fixture.detectChanges();

      mockHelpService.createCategoryArticleAsync.mockRejectedValueOnce(
        new Error('fail')
      );

      const emitSpy = vi.spyOn(component.articleCreated, 'emit');
      await component.createArticle();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.ADD_HELP_ARTICLE,
          result: ActionResult.FAILED,
        })
      );
    });
  });

  describe('updateArticle', () => {
    it('should update article and emit success', async () => {
      fixture.componentRef.setInput('articleId', 'article-1');
      fixture.detectChanges();
      await fixture.whenStable();

      component.newArticlForm.patchValue({ title: { en: 'Updated' } });
      component.model = { en: '<p>Updated content</p>' };

      const emitSpy = vi.spyOn(component.articleUpdated, 'emit');
      await component.updateArticle();

      expect(mockHelpService.updateHelpArticleAsync).toHaveBeenCalledWith({
        id: 'article-1',
        helpArticle: expect.objectContaining({
          content: { en: '<p>Updated content</p>' },
        }),
      });
      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.UPDATE_HELP_ARTICLE,
          result: ActionResult.SUCCEED,
        })
      );
      expect(component.creating()).toBe(false);
    });

    it('should emit failed result on error', async () => {
      fixture.componentRef.setInput('articleId', 'article-1');
      fixture.detectChanges();
      await fixture.whenStable();

      mockHelpService.updateHelpArticleAsync.mockRejectedValueOnce(
        new Error('fail')
      );

      const emitSpy = vi.spyOn(component.articleUpdated, 'emit');
      await component.updateArticle();

      expect(emitSpy).toHaveBeenCalledWith(
        expect.objectContaining({
          type: ActionType.UPDATE_HELP_ARTICLE,
          result: ActionResult.FAILED,
        })
      );
    });
  });

  describe('prepareForm', () => {
    it('should load article and patch form in edit mode', async () => {
      fixture.componentRef.setInput('articleId', 'article-1');
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.editMode()).toBe(true);
      expect(component.articleToEdit).toEqual(mockArticle);
      expect(component.model).toEqual({ en: '<p>Test content</p>' });
    });
  });
});
