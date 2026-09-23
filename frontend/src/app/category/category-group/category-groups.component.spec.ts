import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { CategoryService, InterestGroup } from 'app/core/generated/circabc';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { CategoryGroupsComponent } from './category-groups.component';

const mockGroups: InterestGroup[] = [
  { name: 'GroupB', title: { en: 'Beta' }, permissions: {} },
  { name: 'GroupA', title: { en: 'Alpha' }, permissions: {} },
];

describe('CategoryGroupsComponent', () => {
  let component: CategoryGroupsComponent;
  let fixture: ComponentFixture<CategoryGroupsComponent>;
  let paramsSubject: Subject<{ id: string }>;

  const mockCategoryService = {
    getInterestGroupsByCategoryIdAsync: vi
      .fn()
      .mockResolvedValue([...mockGroups]),
  };

  const mockUiMessageService = {
    addErrorMessage: vi.fn(),
  };

  const mockI18nPipe = {
    transform: vi.fn((mltext: { [key: string]: string } | undefined) => {
      if (!mltext) return '';
      return mltext['en'] ?? '';
    }),
  };

  beforeEach(async () => {
    paramsSubject = new Subject();

    await TestBed.configureTestingModule({
      imports: [CategoryGroupsComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: UiMessageService, useValue: mockUiMessageService },
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

    fixture = TestBed.createComponent(CategoryGroupsComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load and sort interest groups on route param change', async () => {
    mockCategoryService.getInterestGroupsByCategoryIdAsync.mockResolvedValue([
      ...mockGroups,
    ]);
    fixture.detectChanges();
    paramsSubject.next({ id: 'cat1' });
    await fixture.whenStable();

    expect(
      mockCategoryService.getInterestGroupsByCategoryIdAsync
    ).toHaveBeenCalledWith({ id: 'cat1', language: 'en' });
    expect(component.interestGroups()).toHaveLength(2);
    // Sorted alphabetically by title
    expect(component.interestGroups()[0].name).toBe('GroupA');
    expect(component.interestGroups()[1].name).toBe('GroupB');
  });

  it('should not call service when categoryId is falsy', async () => {
    mockCategoryService.getInterestGroupsByCategoryIdAsync.mockClear();
    fixture.detectChanges();
    paramsSubject.next({ id: '' });
    await fixture.whenStable();

    expect(
      mockCategoryService.getInterestGroupsByCategoryIdAsync
    ).not.toHaveBeenCalled();
  });

  it('should handle error and call uiMessageService', async () => {
    const error = 'Network error';
    mockCategoryService.getInterestGroupsByCategoryIdAsync.mockRejectedValue(
      error
    );
    fixture.detectChanges();
    paramsSubject.next({ id: 'cat1' });
    await fixture.whenStable();

    expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(error);
  });

  describe('getNameOrTitle', () => {
    it('should return transformed title when title has keys', () => {
      const group: InterestGroup = {
        name: 'test',
        title: { en: 'English Title' },
        permissions: {},
      };
      mockI18nPipe.transform.mockReturnValue('English Title');

      expect(component.getNameOrTitle(group)).toBe('English Title');
    });

    it('should return name when title is empty', () => {
      const group: InterestGroup = {
        name: 'fallback',
        title: {},
        permissions: {},
      };

      expect(component.getNameOrTitle(group)).toBe('fallback');
    });

    it('should return name when i18nPipe returns empty string', () => {
      const group: InterestGroup = {
        name: 'fallback',
        title: { en: '' },
        permissions: {},
      };
      mockI18nPipe.transform.mockReturnValue('');

      expect(component.getNameOrTitle(group)).toBe('fallback');
    });
  });
});
