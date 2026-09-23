import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import { ActionEmitterResult, ActionResult } from 'app/action-result';
import { CategoryService, User } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { UrlHelperService } from 'app/core/url-helper.service';
import { SERVER_URL } from 'app/core/variables';
import { DownloadUtilService } from 'app/shared/services/download-util.service';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { CategoryAdministratorsComponent } from './category-administrators.component';

const mockAdmins: User[] = [
  { userId: 'admin1', firstname: 'John', lastname: 'Doe' },
  { userId: 'admin2', firstname: 'Jane', lastname: 'Smith' },
];

describe('CategoryAdministratorsComponent', () => {
  let component: CategoryAdministratorsComponent;
  const paramsSubject = new Subject<{ id: string }>();

  const mockCategoryService = {
    getCategoryAdministratorsAsync: vi.fn().mockResolvedValue(mockAdmins),
    deleteCategoryAdministartor: vi.fn().mockReturnValue(of(undefined)),
    deleteCategoryAdministartorAsync: vi.fn().mockResolvedValue(undefined),
  };

  const mockLoginService = {
    getCurrentUsername: vi.fn().mockReturnValue('currentUser'),
  };

  const mockUiMessageService = {
    addSuccessMessage: vi.fn(),
    addErrorMessage: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryAdministratorsComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { params: paramsSubject.asObservable() },
        },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: UiMessageService, useValue: mockUiMessageService },
        { provide: SERVER_URL, useValue: 'http://localhost/' },
        {
          provide: DownloadUtilService,
          useValue: { getDownloadUrl: vi.fn().mockReturnValue('http://mock') },
        },
        {
          provide: UrlHelperService,
          useValue: { get: vi.fn().mockReturnValue(of('blob:mock')) },
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

    const fixture = TestBed.createComponent(CategoryAdministratorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('should load administrators on route param change', async () => {
    paramsSubject.next({ id: 'cat1' });
    await new Promise((resolve) => setTimeout(resolve));
    expect(
      mockCategoryService.getCategoryAdministratorsAsync
    ).toHaveBeenCalledWith({
      id: 'cat1',
    });
    expect(component.administrators()).toEqual(mockAdmins);
    expect(component.loading).toBe(false);
  });

  it('should set loading to false even on error', async () => {
    mockCategoryService.getCategoryAdministratorsAsync.mockRejectedValueOnce(
      new Error('fail')
    );
    paramsSubject.next({ id: 'cat2' });
    await new Promise((resolve) => setTimeout(resolve));
    expect(component.loading).toBe(false);
  });

  describe('canDeleteAdmin', () => {
    beforeEach(() => {
      component.administrators.set(mockAdmins);
      mockLoginService.getCurrentUsername.mockReturnValue('currentUser');
    });

    it('should return true when more than one admin and not the current user', () => {
      expect(component.canDeleteAdmin({ userId: 'admin1' })).toBe(true);
    });

    it('should return false when the admin is the current user', () => {
      mockLoginService.getCurrentUsername.mockReturnValue('admin1');
      expect(component.canDeleteAdmin({ userId: 'admin1' })).toBe(false);
    });

    it('should return false when there is only one administrator', () => {
      component.administrators.set([{ userId: 'admin1' }]);
      expect(component.canDeleteAdmin({ userId: 'admin1' })).toBe(false);
    });
  });

  describe('uninviteUser', () => {
    beforeEach(() => {
      component.categoryId.set('cat1');
      component.administrators.set(mockAdmins);
    });

    it('should delete admin and reload list on success', async () => {
      await component.uninviteUser({ userId: 'admin1' });
      expect(
        mockCategoryService.deleteCategoryAdministartorAsync
      ).toHaveBeenCalledWith({ id: 'cat1', userId: 'admin1' });
      expect(
        mockCategoryService.getCategoryAdministratorsAsync
      ).toHaveBeenCalledWith({ id: 'cat1' });
      expect(mockUiMessageService.addSuccessMessage).toHaveBeenCalled();
    });

    it('should show error message on failure', async () => {
      mockCategoryService.deleteCategoryAdministartorAsync.mockRejectedValueOnce(
        new Error('fail')
      );
      await component.uninviteUser({ userId: 'admin1' });
      expect(mockUiMessageService.addErrorMessage).toHaveBeenCalled();
    });

    it('should do nothing if user has no userId', async () => {
      await component.uninviteUser({});
      expect(
        mockCategoryService.deleteCategoryAdministartorAsync
      ).not.toHaveBeenCalled();
    });
  });

  describe('refresh', () => {
    beforeEach(() => {
      component.categoryId.set('cat1');
      component.showModal.set(true);
    });

    it('should reload and close modal on SUCCEED', async () => {
      const res: ActionEmitterResult = { result: ActionResult.SUCCEED };
      await component.refresh(res);
      expect(
        mockCategoryService.getCategoryAdministratorsAsync
      ).toHaveBeenCalledWith({ id: 'cat1' });
      expect(component.showModal()).toBe(false);
    });

    it('should close modal on CANCELED', async () => {
      const res: ActionEmitterResult = { result: ActionResult.CANCELED };
      await component.refresh(res);
      expect(component.showModal()).toBe(false);
    });

    it('should not close modal on FAILED', async () => {
      const res: ActionEmitterResult = { result: ActionResult.FAILED };
      await component.refresh(res);
      expect(component.showModal()).toBe(true);
    });
  });
});
