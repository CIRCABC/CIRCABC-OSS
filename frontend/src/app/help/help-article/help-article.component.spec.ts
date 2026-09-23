import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import { HelpService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of, ReplaySubject } from 'rxjs';
import { vi } from 'vitest';
import { HelpArticleComponent } from './help-article.component';

describe('HelpArticleComponent', () => {
  let component: HelpArticleComponent;
  let fixture: ComponentFixture<HelpArticleComponent>;
  let paramsSubject: ReplaySubject<Record<string, string>>;

  const mockHelpService = {
    getHelpCategoriesAsync: vi.fn().mockResolvedValue([]),
    getHelpCategoryAsync: vi
      .fn()
      .mockResolvedValue({ id: 'cat1', title: { en: 'Cat 1' } }),
    getCategoryArticlesAsync: vi.fn().mockResolvedValue([]),
    getHelpArticleAsync: vi.fn().mockResolvedValue({
      id: 'art1',
      title: { en: 'Article' },
      content: { en: '<p>Hello</p>' },
    }),
    toggleHighlightArticle: vi
      .fn()
      .mockReturnValue(of({ id: 'art1', highlighted: true })),
    toggleHighlightArticleAsync: vi
      .fn()
      .mockResolvedValue({ id: 'art1', highlighted: true }),
  };

  const mockLoginService = {
    isGuest: vi.fn().mockReturnValue(true),
    getUser: vi.fn().mockReturnValue({
      properties: { isAdmin: 'false', isCircabcAdmin: 'false' },
    }),
  };

  const mockRouter = {
    navigate: vi.fn().mockResolvedValue(true),
  };

  beforeEach(async () => {
    // ReplaySubject so route.params always has a current value, matching
    // toSignal's synchronous initial-value expectations.
    paramsSubject = new ReplaySubject(1);
    paramsSubject.next({});

    await TestBed.configureTestingModule({
      imports: [HelpArticleComponent],
      providers: [
        provideRouter([]),
        { provide: HelpService, useValue: mockHelpService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HelpArticleComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load categories on init', async () => {
    const categories = [{ id: 'cat1', title: { en: 'Cat' } }];
    mockHelpService.getHelpCategoriesAsync.mockResolvedValue(categories);

    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockHelpService.getHelpCategoriesAsync).toHaveBeenCalled();
    expect(component.categories()).toEqual(categories);
    expect(component.loading()).toBe(false);
  });

  it('should load category and article when params emit', async () => {
    mockHelpService.getHelpCategoriesAsync.mockResolvedValue([]);
    fixture.detectChanges();
    await fixture.whenStable();

    paramsSubject.next({ categoryId: 'cat1', articleId: 'art1' });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockHelpService.getHelpCategoryAsync).toHaveBeenCalledWith({
      id: 'cat1',
    });
    expect(mockHelpService.getHelpArticleAsync).toHaveBeenCalledWith({
      id: 'art1',
    });
  });

  describe('isAdminOrSupport', () => {
    it('should return false for guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isAdminOrSupport()).toBe(false);
    });

    it('should return true for admin user', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getUser.mockReturnValue({
        properties: { isAdmin: 'true' },
      });
      expect(component.isAdminOrSupport()).toBe(true);
    });

    it('should return true for circabc admin', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getUser.mockReturnValue({
        properties: { isCircabcAdmin: 'true' },
      });
      expect(component.isAdminOrSupport()).toBe(true);
    });

    it('should return false for non-admin user', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getUser.mockReturnValue({
        properties: { isAdmin: 'false', isCircabcAdmin: 'false' },
      });
      expect(component.isAdminOrSupport()).toBe(false);
    });
  });

  describe('getTitle', () => {
    it('should return empty string when title is undefined', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ articleId: 'no-title' });
      mockHelpService.getHelpArticleAsync.mockResolvedValue({
        id: 'no-title',
      });
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.getTitle()).toBe('');
    });

    it('should return title in active language', async () => {
      mockHelpService.getHelpArticleAsync.mockResolvedValue({
        id: 'art1',
        title: { en: 'English Title', fr: 'French Title' },
      });
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ articleId: 'art1' });
      fixture.detectChanges();
      await fixture.whenStable();

      const translocoService = TestBed.inject(TranslocoService);
      translocoService.setActiveLang('en');
      expect(component.getTitle()).toBe('English Title');
    });

    it('should fallback to default language', async () => {
      mockHelpService.getHelpArticleAsync.mockResolvedValue({
        id: 'art1',
        title: { en: 'Default Title' },
      });
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ articleId: 'art1' });
      fixture.detectChanges();
      await fixture.whenStable();

      const translocoService = TestBed.inject(TranslocoService);
      translocoService.setActiveLang('fr');
      expect(component.getTitle()).toBe('Default Title');
    });

    it('should fallback to first available key', async () => {
      mockHelpService.getHelpArticleAsync.mockResolvedValue({
        id: 'art1',
        title: { de: 'German Title' },
      });
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ articleId: 'art1' });
      fixture.detectChanges();
      await fixture.whenStable();

      const translocoService = TestBed.inject(TranslocoService);
      translocoService.setActiveLang('fr');
      expect(component.getTitle()).toBe('German Title');
    });
  });

  describe('getContent', () => {
    it('should return empty string when content is undefined', async () => {
      mockHelpService.getHelpArticleAsync.mockResolvedValue({ id: 'art1' });
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ articleId: 'art1' });
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.getContent()).toBe('');
    });

    it('should return sanitized HTML for active language', async () => {
      mockHelpService.getHelpArticleAsync.mockResolvedValue({
        id: 'art1',
        content: { en: '<p>Hello</p>' },
      });
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ articleId: 'art1' });
      fixture.detectChanges();
      await fixture.whenStable();

      const translocoService = TestBed.inject(TranslocoService);
      translocoService.setActiveLang('en');
      const result = component.getContent();
      expect(result).toBeDefined();
      // Result is a SafeHtml object, not a plain string
      expect(result).not.toBe('');
    });
  });

  describe('redirectAfterDeletion', () => {
    it('should navigate on success', async () => {
      await component.redirectAfterDeletion({ result: ActionResult.SUCCEED });
      expect(mockRouter.navigate).toHaveBeenCalled();
    });

    it('should not navigate on cancel', async () => {
      mockRouter.navigate.mockClear();
      await component.redirectAfterDeletion({ result: ActionResult.CANCELED });
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });
  });

  describe('toggleHighlight', () => {
    it('should call toggleHighlightArticle and update article', async () => {
      const updated = { id: 'art1', highlighted: true };
      mockHelpService.getHelpArticleAsync.mockResolvedValue({
        id: 'art1',
        highlighted: false,
      });
      mockHelpService.toggleHighlightArticleAsync.mockResolvedValue(updated);
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ articleId: 'art1' });
      fixture.detectChanges();
      await fixture.whenStable();

      await component.toggleHighlight();

      expect(mockHelpService.toggleHighlightArticleAsync).toHaveBeenCalledWith({
        id: 'art1',
      });
      expect(component.article()).toEqual(updated);
    });
  });

  describe('refresh', () => {
    it('should reload article after edit', async () => {
      mockHelpService.getHelpArticleAsync.mockResolvedValue({
        id: 'art1',
        title: { en: 'Original' },
      });
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ articleId: 'art1' });
      fixture.detectChanges();
      await fixture.whenStable();

      const refreshed = { id: 'art1', title: { en: 'Refreshed' } };
      mockHelpService.getHelpArticleAsync.mockResolvedValue(refreshed);

      component.refresh({ result: ActionResult.SUCCEED });
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockHelpService.getHelpArticleAsync).toHaveBeenCalledWith({
        id: 'art1',
      });
      expect(component.article()).toEqual(refreshed);
    });
  });

  describe('article loading error', () => {
    it('should set loadingError on failure', async () => {
      mockHelpService.getHelpArticleAsync.mockRejectedValue(new Error('fail'));
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ articleId: 'bad-id' });
      fixture.detectChanges();
      await fixture.whenStable();

      expect(component.loadingError()).toBe(true);
    });
  });
});
