import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import { AnalyticsService } from 'app/core/analytics.service';
import {
  CategoryService,
  Header,
  HeaderService,
  InterestGroup,
  Node as ModelNode,
  NodesService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ExplorerComponent } from './explorer.component';

describe('ExplorerComponent', () => {
  let component: ExplorerComponent;
  let fixture: ComponentFixture<ExplorerComponent>;

  const mockHeaderService = {
    getHeadersAsync: vi.fn().mockResolvedValue([]),
    getHeaderAsync: vi.fn().mockResolvedValue({}),
    getCategoriesByHeaderIdAsync: vi.fn().mockResolvedValue([]),
  };

  const mockCategoryService = {
    getCategoryAsync: vi.fn().mockResolvedValue({}),
    getInterestGroupsByCategoryIdAsync: vi.fn().mockResolvedValue([]),
  };

  const mockNodesService = {
    getNodeAsync: vi.fn().mockResolvedValue({}),
  };

  const mockLoginService = {
    getUser: vi
      .fn()
      .mockReturnValue({ userId: 'guest', properties: undefined }),
    isGuest: vi.fn().mockReturnValue(true),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  const mockRouter = {
    navigate: vi.fn(),
  };

  const mockRoute = {
    queryParamMap: of({
      has: () => false,
      get: () => null,
    }),
  };

  const mockAnalyticsService = {
    trackSiteSearch: vi.fn(),
  };

  const mockI18nPipe = {
    transform: vi.fn().mockReturnValue(''),
  };

  const mockTranslocoService = {
    getActiveLang: vi.fn().mockReturnValue('en'),
    getDefaultLang: vi.fn().mockReturnValue('en'),
    selectTranslation: vi.fn().mockReturnValue(of({})),
    langChanges$: of('en'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExplorerComponent],
      providers: [
        provideRouter([]),
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: HeaderService, useValue: mockHeaderService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: NodesService, useValue: mockNodesService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: AnalyticsService, useValue: mockAnalyticsService },
        { provide: I18nPipe, useValue: mockI18nPipe },
        { provide: TranslocoService, useValue: mockTranslocoService },
      ],
    }).compileComponents();

    TestBed.overrideComponent(ExplorerComponent, {
      set: { template: '' },
    });

    fixture = TestBed.createComponent(ExplorerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  describe('isStepHeaders', () => {
    it('should return true when step is headers', () => {
      component.step.set('headers');
      expect(component.isStepHeaders()).toBe(true);
    });

    it('should return false when step is not headers', () => {
      component.step.set('categories');
      expect(component.isStepHeaders()).toBe(false);
    });
  });

  describe('isStepCategories', () => {
    it('should return true when step is categories', () => {
      component.step.set('categories');
      expect(component.isStepCategories()).toBe(true);
    });
  });

  describe('isStepGroups', () => {
    it('should return true when step is groups', () => {
      component.step.set('groups');
      expect(component.isStepGroups()).toBe(true);
    });
  });

  describe('reset', () => {
    it('should reset step to headers and clear state', () => {
      component.step.set('groups');
      component.currentHeader.set({ name: 'Test' });
      component.currentCategory.set({ id: '1', name: 'Cat' });

      component.reset();

      expect(component.step()).toBe('headers');
      expect(component.currentHeader()).toBeUndefined();
      expect(component.currentCategory()).toBeUndefined();
    });
  });

  describe('isAdminOrCircabcAdmin', () => {
    it('should return false for guest user', () => {
      mockLoginService.getUser.mockReturnValue({
        userId: 'guest',
        properties: undefined,
      });
      expect(component.isAdminOrCircabcAdmin()).toBe(false);
    });

    it('should return false when userId is empty', () => {
      mockLoginService.getUser.mockReturnValue({ userId: '', properties: {} });
      expect(component.isAdminOrCircabcAdmin()).toBe(false);
    });

    it('should return true when user is admin', () => {
      mockLoginService.getUser.mockReturnValue({
        userId: 'admin',
        properties: { isAdmin: 'true', isCircabcAdmin: 'false' },
      });
      expect(component.isAdminOrCircabcAdmin()).toBe(true);
    });

    it('should return true when user is circabc admin', () => {
      mockLoginService.getUser.mockReturnValue({
        userId: 'cadmin',
        properties: { isAdmin: 'false', isCircabcAdmin: 'true' },
      });
      expect(component.isAdminOrCircabcAdmin()).toBe(true);
    });
  });

  describe('isLoggedIn', () => {
    it('should return false when user is guest', () => {
      mockLoginService.isGuest.mockReturnValue(true);
      expect(component.isLoggedIn()).toBe(false);
    });

    it('should return true when user is not guest', () => {
      mockLoginService.isGuest.mockReturnValue(false);
      expect(component.isLoggedIn()).toBe(true);
    });
  });

  describe('openInterestGroup', () => {
    it('should navigate to the group route', () => {
      const ig: InterestGroup = {
        name: 'TestIG',
        permissions: {},
        id: 'ig-id',
      };
      component.openInterestGroup(ig);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/group', 'ig-id']);
    });
  });

  describe('getHeaderImgName', () => {
    it('should return court of justice image for Justice European header', () => {
      const header: Header = { name: 'Court of Justice of the European Union' };
      expect(component.getHeaderImgName(header)).toBe(
        'img/court-of-justice-128.png'
      );
    });

    it('should return commission image for Commission European header', () => {
      const header: Header = { name: 'European Commission' };
      expect(component.getHeaderImgName(header)).toBe(
        'img/LOGO-CE_Vertical_EN_128.png'
      );
    });

    it('should return parliament image for Parliament European header', () => {
      const header: Header = { name: 'European Parliament' };
      expect(component.getHeaderImgName(header)).toBe('img/EP-logo-128.png');
    });

    it('should return euro institutions image for Programmes European header', () => {
      const header: Header = { name: 'European Programmes' };
      expect(component.getHeaderImgName(header)).toBe(
        'img/euro-institutions-128.png'
      );
    });

    it('should return default circabc image for other headers', () => {
      const header: Header = { name: 'Other Header' };
      expect(component.getHeaderImgName(header)).toBe(
        'img/LOGO_circabc_header-128.png'
      );
    });
  });

  describe('hasCategoryLogo', () => {
    it('should return true when logoRef is set', () => {
      expect(
        component.hasCategoryLogo({ name: 'Cat', logoRef: 'some-ref' })
      ).toBe(true);
    });

    it('should return false when logoRef is undefined', () => {
      expect(component.hasCategoryLogo({ name: 'Cat' })).toBe(false);
    });

    it('should return false when logoRef is empty', () => {
      expect(component.hasCategoryLogo({ name: 'Cat', logoRef: '' })).toBe(
        false
      );
    });
  });

  describe('hasModelNodeLogo', () => {
    it('should return true when properties.logoRef is set', () => {
      const node: ModelNode = { properties: { logoRef: 'ref-123' } };
      expect(component.hasModelNodeLogo(node)).toBe(true);
    });

    it('should return false when properties is undefined', () => {
      const node: ModelNode = {};
      expect(component.hasModelNodeLogo(node)).toBe(false);
    });

    it('should return false when logoRef is empty', () => {
      const node: ModelNode = { properties: { logoRef: '' } };
      expect(component.hasModelNodeLogo(node)).toBe(false);
    });
  });

  describe('getLogoRef', () => {
    it('should return last part of logoRef path', () => {
      const node: ModelNode = { properties: { logoRef: 'path/to/logo.png' } };
      expect(component.getLogoRef(node)).toBe('logo.png');
    });

    it('should return empty string when no properties', () => {
      const node: ModelNode = {};
      expect(component.getLogoRef(node)).toBe('');
    });
  });

  describe('getCategoryGroupDescription', () => {
    it('should return name when title is not set', () => {
      const node: ModelNode = { name: 'CatName' };
      expect(component.getCategoryGroupDescription(node)).toBe('CatName');
    });

    it('should return empty string when name is undefined', () => {
      const node: ModelNode = {};
      expect(component.getCategoryGroupDescription(node)).toBe('');
    });

    it('should return i18n title when available', () => {
      mockI18nPipe.transform.mockReturnValue('Translated Title');
      const node: ModelNode = {
        name: 'CatName',
        title: { en: 'Translated Title' },
      };
      expect(component.getCategoryGroupDescription(node)).toBe(
        'Translated Title'
      );
    });
  });

  describe('getInterestGroupDescription', () => {
    it('should return name when title is undefined', () => {
      const ig: InterestGroup = { name: 'IGName', permissions: {} };
      expect(component.getInterestGroupDescription(ig)).toBe('IGName');
    });

    it('should return i18n title when available', () => {
      mockI18nPipe.transform.mockReturnValue('IG Title');
      const ig: InterestGroup = {
        name: 'IGName',
        permissions: {},
        title: { en: 'IG Title' },
      };
      expect(component.getInterestGroupDescription(ig)).toBe('IG Title');
    });

    it('should return name when title transform returns empty', () => {
      mockI18nPipe.transform.mockReturnValue('');
      const ig: InterestGroup = {
        name: 'IGName',
        permissions: {},
        title: { en: '' },
      };
      expect(component.getInterestGroupDescription(ig)).toBe('IGName');
    });
  });

  describe('isCategoryAdmin', () => {
    it('should return false when currentCategory is undefined', () => {
      component.currentCategory.set(undefined);
      expect(component.isCategoryAdmin()).toBe(false);
    });

    it('should return true when CircaCategoryAdmin is ALLOWED', () => {
      component.currentCategory.set({
        permissions: { CircaCategoryAdmin: 'ALLOWED' },
      });
      expect(component.isCategoryAdmin()).toBe(true);
    });

    it('should return false when CircaCategoryAdmin is not ALLOWED', () => {
      component.currentCategory.set({
        permissions: { CircaCategoryAdmin: 'DENIED' },
      });
      expect(component.isCategoryAdmin()).toBe(false);
    });
  });

  describe('filterGroups', () => {
    beforeEach(() => {
      component.interestGroups = [
        { name: 'Alpha Group', permissions: {}, title: { en: 'Alpha Group' } },
        { name: 'Beta Group', permissions: {}, title: { en: 'Beta Group' } },
        { name: 'Gamma Group', permissions: {}, title: { en: 'Gamma Group' } },
      ];
      mockI18nPipe.transform.mockImplementation(
        (mltext: { [key: string]: string } | undefined) => {
          if (mltext?.['en']) {
            return mltext['en'];
          }
          return '';
        }
      );
    });

    it('should include all groups when search is empty', () => {
      component.filterGroups('');
      const keys = component.getKeysOfGroups();
      expect(keys.length).toBeGreaterThan(0);
    });

    it('should filter groups by name', () => {
      component.filterGroups('Alpha');
      const keys = component.getKeysOfGroups();
      expect(keys).toEqual(['A']);
      expect(component.getGroupInIndex('A')).toHaveLength(1);
    });

    it('should track analytics when search is provided', () => {
      component.filterGroups('Beta');
      expect(mockAnalyticsService.trackSiteSearch).toHaveBeenCalledWith(
        'Beta',
        'group-explorer',
        expect.any(Number)
      );
    });
  });

  describe('listCategories', () => {
    it('should do nothing when header is undefined', async () => {
      await component.listCategories(undefined);
      expect(
        mockHeaderService.getCategoriesByHeaderIdAsync
      ).not.toHaveBeenCalled();
    });

    it('should load categories for a header', async () => {
      const header: Header = { name: 'Test Header', id: 'header-1' };
      mockHeaderService.getCategoriesByHeaderIdAsync.mockResolvedValue([]);

      await component.listCategories(header);

      expect(component.step()).toBe('categories');
      expect(component.currentHeader()).toBe(header);
      expect(
        mockHeaderService.getCategoriesByHeaderIdAsync
      ).toHaveBeenCalledWith({
        id: 'header-1',
        language: 'en',
      });
    });
  });

  describe('listInterestGroups', () => {
    it('should do nothing when category is undefined', async () => {
      await component.listInterestGroups(undefined);
      expect(
        mockCategoryService.getInterestGroupsByCategoryIdAsync
      ).not.toHaveBeenCalled();
    });

    it('should load interest groups for a category', async () => {
      const category: ModelNode = { id: 'cat-1', name: 'Category 1' };
      mockNodesService.getNodeAsync.mockResolvedValue(category);
      mockCategoryService.getInterestGroupsByCategoryIdAsync.mockResolvedValue(
        []
      );

      await component.listInterestGroups(category);

      expect(component.step()).toBe('groups');
      expect(mockNodesService.getNodeAsync).toHaveBeenCalledWith({
        id: 'cat-1',
      });
      expect(
        mockCategoryService.getInterestGroupsByCategoryIdAsync
      ).toHaveBeenCalledWith({ id: 'cat-1', language: 'en' });
    });
  });

  describe('shouldDisplayMenu', () => {
    it('should return false for non-admin guest', () => {
      mockLoginService.getUser.mockReturnValue({
        userId: 'guest',
        properties: undefined,
      });
      component.currentCategory.set(undefined);
      expect(component.shouldDisplayMenu()).toBe(false);
    });

    it('should return true for admin user', () => {
      mockLoginService.getUser.mockReturnValue({
        userId: 'admin',
        properties: { isAdmin: 'true', isCircabcAdmin: 'false' },
      });
      expect(component.shouldDisplayMenu()).toBe(true);
    });

    it('should return true for category admin with current category', () => {
      mockLoginService.getUser.mockReturnValue({
        userId: 'user1',
        properties: { isAdmin: 'false', isCircabcAdmin: 'false' },
      });
      component.currentCategory.set({
        permissions: { CircaCategoryAdmin: 'ALLOWED' },
      });
      expect(component.shouldDisplayMenu()).toBeTruthy();
    });
  });

  describe('getGroupFirstLetter', () => {
    it('should return first letter of i18n title', () => {
      mockI18nPipe.transform.mockReturnValue('Hello');
      const ig: InterestGroup = {
        name: 'test',
        permissions: {},
        title: { en: 'Hello' },
      };
      expect(component.getGroupFirstLetter(ig)).toBe('H');
    });

    it('should return first letter of name when title is empty', () => {
      const ig: InterestGroup = { name: 'Zeta', permissions: {} };
      expect(component.getGroupFirstLetter(ig)).toBe('Z');
    });
  });
});
