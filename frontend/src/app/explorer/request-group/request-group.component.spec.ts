import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import {
  provideTransloco,
  TRANSLOCO_LOADER,
  TranslocoModule,
} from '@jsverse/transloco';
import {
  Category,
  CategoryService,
  HeaderService,
  User,
  UserService,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { I18nPipe } from 'app/shared/pipes/i18n.pipe';
import { of, Subject } from 'rxjs';
import { vi } from 'vitest';
import { RequestGroupComponent } from './request-group.component';

const mockHeaders = [
  { id: 'h1', name: 'Header 1' },
  { id: 'h2', name: 'Header 2' },
];

const mockCategories: Category[] = [
  { id: 'c1', name: 'Category 1', title: { en: 'Cat 1' } },
  { id: 'c2', name: 'Category 2' },
];

const mockUsers: User[] = [
  { userId: 'user1', firstname: 'John', lastname: 'Doe' },
  { userId: 'user2', firstname: 'Jane', lastname: 'Smith' },
];

const mockUser: User = {
  userId: 'currentUser',
  firstname: 'Current',
  lastname: 'User',
};

describe('RequestGroupComponent', () => {
  let component: RequestGroupComponent;
  let fixture: ComponentFixture<RequestGroupComponent>;
  let queryParamsSubject: Subject<Record<string, string>>;

  const mockHeaderService = {
    getHeadersAsync: vi.fn().mockResolvedValue(mockHeaders),
    getCategoriesByHeaderIdAsync: vi.fn().mockResolvedValue(mockCategories),
  };

  const mockCategoryService = {
    postRequestInterestGroup: vi.fn().mockReturnValue(of({})),
    postRequestInterestGroupAsync: vi.fn().mockResolvedValue({}),
  };

  const mockUserService = {
    getUsersAsync: vi.fn().mockResolvedValue(mockUsers),
  };

  const mockLoginService = {
    getUser: vi.fn().mockReturnValue(mockUser),
  };

  const mockRouter = {
    navigate: vi.fn(),
  };

  const mockI18nPipe = {
    transform: vi.fn().mockReturnValue('Translated'),
  };

  beforeEach(async () => {
    queryParamsSubject = new Subject<Record<string, string>>();

    await TestBed.configureTestingModule({
      imports: [RequestGroupComponent],
      providers: [
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
        { provide: HeaderService, useValue: mockHeaderService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: { queryParams: queryParamsSubject.asObservable() },
        },
        { provide: I18nPipe, useValue: mockI18nPipe },
      ],
    })
      .overrideComponent(RequestGroupComponent, {
        set: { imports: [TranslocoModule], schemas: [NO_ERRORS_SCHEMA] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(RequestGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load headers on init', () => {
    expect(mockHeaderService.getHeadersAsync).toHaveBeenCalled();
    expect(component.headers()).toEqual(mockHeaders);
  });

  it('should initialize form with required validators', () => {
    expect(component.form).toBeDefined();
    expect(component.form.get('header')).toBeTruthy();
    expect(component.form.get('category')).toBeTruthy();
    expect(component.form.get('name')).toBeTruthy();
    expect(component.form.get('comment')).toBeTruthy();
  });

  it('should load categories when header changes', async () => {
    component.form.controls['header'].setValue('h1');
    fixture.detectChanges();
    await fixture.whenStable();
    expect(mockHeaderService.getCategoriesByHeaderIdAsync).toHaveBeenCalledWith(
      {
        id: 'h1',
      }
    );
    expect(component.categories()).toEqual(mockCategories);
  });

  it('should set selectedCategory when category changes', async () => {
    component.form.controls['header'].setValue('h1');
    fixture.detectChanges();
    await fixture.whenStable();

    component.form.controls['category'].setValue('c1');
    fixture.detectChanges();
    await fixture.whenStable();
    expect(component.selectedCategory()).toEqual(mockCategories[0]);
  });

  it('should set header and category from query params', () => {
    queryParamsSubject.next({ header: 'h1', category: 'c1' });
    expect(component.form.controls['header'].value).toBe('h1');
    expect(component.form.controls['category'].value).toBe('c1');
  });

  describe('getNameOrTitle', () => {
    it('should return translated title when title exists', () => {
      const result = component.getNameOrTitle(mockCategories[0]);
      expect(mockI18nPipe.transform).toHaveBeenCalledWith(
        mockCategories[0].title
      );
      expect(result).toBe('Translated');
    });

    it('should return name when title is empty', () => {
      const category: Category = { name: 'TestCat', title: {} };
      const result = component.getNameOrTitle(category);
      expect(result).toBe('TestCat');
    });

    it('should return name when title is undefined', () => {
      const result = component.getNameOrTitle(mockCategories[1]);
      expect(result).toBe('Category 2');
    });
  });

  describe('searchUsers', () => {
    it('should search users when search value is set', async () => {
      component.groupLeadersForm.controls['search'].setValue('john');
      component.searchUsers();
      fixture.detectChanges();
      await fixture.whenStable();
      expect(mockUserService.getUsersAsync).toHaveBeenCalledWith({
        query: 'john',
      });
      expect(component.availableUsers()).toEqual(mockUsers);
    });

    it('should not search when search value is empty', async () => {
      mockUserService.getUsersAsync.mockClear();
      component.groupLeadersForm.controls['search'].setValue('');
      component.searchUsers();
      fixture.detectChanges();
      await fixture.whenStable();
      expect(mockUserService.getUsersAsync).not.toHaveBeenCalled();
    });
  });

  describe('selectUsers', () => {
    it('should add selected users to futureMembers', async () => {
      component.groupLeadersForm.controls['search'].setValue('john');
      component.searchUsers();
      fixture.detectChanges();
      await fixture.whenStable();

      component.groupLeadersForm.controls['possibleUsers'].setValue(['user1']);
      component.selectUsers();
      expect(component.futureMembers).toEqual([mockUsers[0]]);
    });

    it('should not add duplicate users', async () => {
      component.groupLeadersForm.controls['search'].setValue('john');
      component.searchUsers();
      fixture.detectChanges();
      await fixture.whenStable();

      component.futureMembers = [mockUsers[0]];
      component.groupLeadersForm.controls['possibleUsers'].setValue([
        'user1',
        'user2',
      ]);
      component.selectUsers();
      expect(component.futureMembers).toEqual([mockUsers[0], mockUsers[1]]);
    });
  });

  describe('removeFromFutureMember', () => {
    it('should remove user from futureMembers', () => {
      component.futureMembers = [...mockUsers];
      component.removeFromFutureMember(mockUsers[0]);
      expect(component.futureMembers).toEqual([mockUsers[1]]);
    });
  });

  describe('confirm and revert', () => {
    it('should switch to step2 on confirm', () => {
      component.confirm();
      expect(component.step1).toBe(false);
      expect(component.step2).toBe(true);
    });

    it('should switch back to step1 on revert', () => {
      component.confirm();
      component.revert();
      expect(component.step1).toBe(true);
      expect(component.step2).toBe(false);
    });
  });

  describe('requestGroup', () => {
    it('should submit group creation request', async () => {
      component.form.controls['header'].setValue('h1');
      fixture.detectChanges();
      await fixture.whenStable();

      component.form.controls['category'].setValue('c1');
      fixture.detectChanges();
      await fixture.whenStable();

      component.futureMembers = [mockUsers[0]];
      component.form.patchValue({
        name: 'Test Group',
        title: 'Title',
        description: 'Desc',
        comment: 'Justification',
      });

      await component.requestGroup();

      expect(
        mockCategoryService.postRequestInterestGroupAsync
      ).toHaveBeenCalledWith({
        id: 'c1',
        groupCreationRequest: expect.objectContaining({
          from: mockUser,
          proposedName: 'Test Group',
          proposedTitle: { en: 'Title' },
          proposedDescription: { en: 'Desc' },
          justification: 'Justification',
          categoryRef: 'c1',
          leaders: [mockUsers[0]],
        }),
      });
    });

    it('should not submit when selectedCategory has no id', async () => {
      mockCategoryService.postRequestInterestGroup.mockClear();

      await component.requestGroup();

      expect(
        mockCategoryService.postRequestInterestGroup
      ).not.toHaveBeenCalled();
    });
  });

  describe('resetForm', () => {
    it('should clear available users and form fields', async () => {
      component.groupLeadersForm.controls['search'].setValue('john');
      component.searchUsers();
      fixture.detectChanges();
      await fixture.whenStable();

      component.resetForm();
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.availableUsers()).toEqual([]);
      expect(component.groupLeadersForm.controls['search'].value).toBe('');
      expect(component.groupLeadersForm.controls['possibleUsers'].value).toBe(
        ''
      );
    });
  });

  describe('form control getters', () => {
    it('should return header control', () => {
      expect(component.headerControl).toBe(component.form.controls['header']);
    });

    it('should return category control', () => {
      expect(component.categoryControl).toBe(
        component.form.controls['category']
      );
    });

    it('should return name control', () => {
      expect(component.nameControl).toBe(component.form.controls['name']);
    });

    it('should return comment control', () => {
      expect(component.commentControl).toBe(component.form.controls['comment']);
    });
  });
});
