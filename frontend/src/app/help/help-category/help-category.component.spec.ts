import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionResult } from 'app/action-result';
import {
  HelpArticle,
  HelpCategory,
  HelpService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { of, ReplaySubject } from 'rxjs';
import { vi } from 'vitest';
import { HelpCategoryComponent } from './help-category.component';

const mockCategories: HelpCategory[] = [
  { id: 'cat1', title: { en: 'Category 1' }, numberOfArticles: 2 },
];

const mockCategory: HelpCategory = {
  id: 'cat1',
  title: { en: 'Category 1' },
  numberOfArticles: 2,
};

const mockArticles: HelpArticle[] = [
  { id: 'art1', parentId: 'cat1', title: { en: 'Article 1' } },
];

describe('HelpCategoryComponent', () => {
  let component: HelpCategoryComponent;
  let fixture: ComponentFixture<HelpCategoryComponent>;
  let paramsSubject: ReplaySubject<Record<string, string>>;

  const mockHelpService = {
    getHelpCategoriesAsync: vi.fn().mockResolvedValue(mockCategories),
    getHelpCategoryAsync: vi.fn().mockResolvedValue(mockCategory),
    getCategoryArticlesAsync: vi.fn().mockResolvedValue(mockArticles),
  };

  const mockLoginService = {
    isGuest: vi.fn().mockReturnValue(false),
    getUser: vi.fn().mockReturnValue({
      userId: 'admin',
      properties: { isAdmin: 'true', isCircabcAdmin: 'false' },
    }),
  };

  const mockRouter = {
    navigate: vi.fn(),
  };

  beforeEach(async () => {
    // ReplaySubject so route.params always has a current value, matching
    // toSignal's synchronous initial-value expectations.
    paramsSubject = new ReplaySubject(1);
    paramsSubject.next({});

    await TestBed.configureTestingModule({
      imports: [HelpCategoryComponent],
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

    fixture = TestBed.createComponent(HelpCategoryComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load categories and category on init when route has categoryId', async () => {
    paramsSubject.next({ categoryId: 'cat1' });
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockHelpService.getHelpCategoriesAsync).toHaveBeenCalled();
    expect(mockHelpService.getHelpCategoryAsync).toHaveBeenCalledWith({
      id: 'cat1',
    });
    expect(mockHelpService.getCategoryArticlesAsync).toHaveBeenCalledWith({
      id: 'cat1',
      skipcontent: true,
    });
    expect(component.categories()).toEqual(mockCategories);
    expect(component.category()).toEqual(mockCategory);
    expect(component.articles()).toEqual(mockArticles);
    expect(component.loading()).toBe(false);
  });

  it('should not load category when route has no categoryId', async () => {
    fixture.detectChanges();
    await fixture.whenStable();

    expect(mockHelpService.getHelpCategoryAsync).not.toHaveBeenCalled();
  });

  describe('isAdminOrSupport', () => {
    it('should return true when user is admin', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getUser.mockReturnValue({
        userId: 'admin',
        properties: { isAdmin: 'true', isCircabcAdmin: 'false' },
      });

      expect(component.isAdminOrSupport()).toBe(true);
    });

    it('should return true when user is circabc admin', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getUser.mockReturnValue({
        userId: 'cbcadmin',
        properties: { isAdmin: 'false', isCircabcAdmin: 'true' },
      });

      expect(component.isAdminOrSupport()).toBe(true);
    });

    it('should return false when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);

      expect(component.isAdminOrSupport()).toBe(false);
    });

    it('should return false when user has no admin properties', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getUser.mockReturnValue({
        userId: 'user1',
        properties: { isAdmin: 'false', isCircabcAdmin: 'false' },
      });

      expect(component.isAdminOrSupport()).toBe(false);
    });
  });

  describe('refresh', () => {
    it('should reload category when result is SUCCEED', async () => {
      paramsSubject.next({ categoryId: 'cat1' });
      fixture.detectChanges();
      await fixture.whenStable();
      mockHelpService.getHelpCategoryAsync.mockClear();

      component.refresh({ result: ActionResult.SUCCEED });
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockHelpService.getHelpCategoryAsync).toHaveBeenCalledWith({
        id: 'cat1',
      });
    });

    it('should not reload when result is not SUCCEED', async () => {
      paramsSubject.next({ categoryId: 'cat1' });
      fixture.detectChanges();
      await fixture.whenStable();
      mockHelpService.getHelpCategoryAsync.mockClear();

      component.refresh({ result: ActionResult.CANCELED });
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockHelpService.getHelpCategoryAsync).not.toHaveBeenCalled();
    });
  });

  describe('reload', () => {
    it('should reload categories and category when result is SUCCEED', async () => {
      paramsSubject.next({ categoryId: 'cat1' });
      fixture.detectChanges();
      await fixture.whenStable();
      mockHelpService.getHelpCategoriesAsync.mockClear();
      mockHelpService.getHelpCategoryAsync.mockClear();

      component.reload({ result: ActionResult.SUCCEED });
      fixture.detectChanges();
      await fixture.whenStable();

      expect(mockHelpService.getHelpCategoriesAsync).toHaveBeenCalled();
      expect(mockHelpService.getHelpCategoryAsync).toHaveBeenCalledWith({
        id: 'cat1',
      });
    });
  });

  describe('redirect', () => {
    it('should navigate to parent route', () => {
      component.redirect();

      expect(mockRouter.navigate).toHaveBeenCalledWith(['../../'], {
        relativeTo: expect.anything(),
      });
    });
  });
});
