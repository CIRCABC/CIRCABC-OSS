import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoService,
} from '@jsverse/transloco';
import {
  ActionEmitterResult,
  ActionResult,
  ActionType,
} from 'app/action-result/index';
import { CategoryComponent } from 'app/category/category.component';
import { CategoryService, User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';

describe('CategoryComponent', () => {
  let component: CategoryComponent;
  let fixture: ComponentFixture<CategoryComponent>;
  let paramsSubject: Subject<Record<string, string>>;

  const mockRouter = {
    url: '/category/123/interest-groups',
    navigate: vi.fn(),
    events: of(),
  };

  const mockLoginService = {
    isGuest: vi.fn().mockReturnValue(false),
    getUser: vi.fn().mockReturnValue({ userId: 'admin1' } as User),
  };

  const mockTranslocoService = {
    translate: vi.fn().mockReturnValue('translated-text'),
  };

  const mockUiMessageService = {
    addSuccessMessage: vi.fn(),
    addErrorMessage: vi.fn(),
  };

  const mockCategoryService = {
    getCategoryAdministratorsAsync: vi
      .fn()
      .mockResolvedValue([{ userId: 'admin1' }] as User[]),
  };

  beforeEach(() => {
    paramsSubject = new Subject<Record<string, string>>();

    TestBed.configureTestingModule({
      imports: [CategoryComponent],
      providers: [
        provideRouter([]),
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: LoginService, useValue: mockLoginService },
        { provide: TranslocoService, useValue: mockTranslocoService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: CategoryService, useValue: mockCategoryService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    });

    TestBed.overrideComponent(CategoryComponent, {
      set: { template: '' },
    });

    fixture = TestBed.createComponent(CategoryComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('categoryId / admins loading', () => {
    it('should set categoryId and load admins when route params have id', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ id: 'cat-42' });
      await fixture.whenStable();

      expect(component.categoryId()).toBe('cat-42');
      expect(
        mockCategoryService.getCategoryAdministratorsAsync
      ).toHaveBeenCalledWith({ id: 'cat-42' });
    });

    it('should not load admins when params have no id', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({});
      await fixture.whenStable();

      expect(
        mockCategoryService.getCategoryAdministratorsAsync
      ).not.toHaveBeenCalled();
    });
  });

  describe('checkCurrentRouteActive', () => {
    it('should return true when router url includes the route name', () => {
      expect(component.checkCurrentRouteActive('interest-groups')).toBe(true);
    });

    it('should return false when router url does not include the route name', () => {
      expect(component.checkCurrentRouteActive('administrators')).toBe(false);
    });
  });

  describe('route check methods', () => {
    it('isInterestGroupsRoute should return true for matching url', () => {
      expect(component.isInterestGroupsRoute()).toBe(true);
    });

    it('isDetailsRoute should return false for non-matching url', () => {
      expect(component.isDetailsRoute()).toBe(false);
    });
  });

  describe('isCategoryAdmin', () => {
    it('should return true when user is in admins list', async () => {
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ id: '123' });
      await fixture.whenStable();

      expect(component.isCategoryAdmin()).toBe(true);
    });

    it('should return false when user is guest', async () => {
      mockLoginService.isGuest.mockReturnValue(true);
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ id: '123' });
      await fixture.whenStable();

      expect(
        mockCategoryService.getCategoryAdministratorsAsync
      ).toHaveBeenCalled();
      expect(component.isCategoryAdmin()).toBe(false);
    });

    it('should return false when user is not in admins list', async () => {
      mockLoginService.isGuest.mockReturnValue(false);
      mockLoginService.getUser.mockReturnValue({
        userId: 'other-user',
      } as User);
      fixture.detectChanges();
      await fixture.whenStable();
      paramsSubject.next({ id: '123' });
      await fixture.whenStable();

      expect(
        mockCategoryService.getCategoryAdministratorsAsync
      ).toHaveBeenCalled();
      expect(component.isCategoryAdmin()).toBe(false);
    });
  });

  describe('onCreateGroupClosed', () => {
    it('should hide modal when create group is canceled', async () => {
      component.showModalCreate = true;
      const res: ActionEmitterResult = {
        type: ActionType.CREATE_INTEREST_GROUP,
        result: ActionResult.CANCELED,
      };

      await component.onCreateGroupClosed(res);

      expect(component.showModalCreate).toBe(false);
    });

    it('should hide modal, show success, and navigate on successful creation', async () => {
      component.showModalCreate = true;
      const res: ActionEmitterResult = {
        type: ActionType.CREATE_INTEREST_GROUP,
        result: ActionResult.SUCCEED,
        node: { id: 'new-group-id' },
      };

      await component.onCreateGroupClosed(res);

      expect(component.showModalCreate).toBe(false);
      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalledWith(
        'translated-text',
        true
      );
      expect(mockRouter.navigate).toHaveBeenCalledWith([
        'group',
        'new-group-id',
      ]);
    });

    it('should show error message when group already exists', async () => {
      const res: ActionEmitterResult = {
        type: ActionType.CREATE_INTEREST_GROUP_EXISTS,
        result: ActionResult.FAILED,
      };

      await component.onCreateGroupClosed(res);

      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(
        'translated-text',
        false
      );
    });

    it('should hide modal and show error on creation failure', async () => {
      component.showModalCreate = true;
      const res: ActionEmitterResult = {
        type: ActionType.CREATE_INTEREST_GROUP,
        result: ActionResult.FAILED,
      };

      await component.onCreateGroupClosed(res);

      expect(component.showModalCreate).toBe(false);
      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalledWith(
        'translated-text',
        true
      );
    });
  });
});
