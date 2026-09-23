import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTransloco, TRANSLOCO_LOADER } from '@jsverse/transloco';
import {
  CategoryService,
  HeaderService,
  UserService,
} from 'app/core/generated/circabc';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { CreateCategoryComponent } from './create-category.component';

const mockHeaders = [{ name: 'Header1' }, { name: 'Header2' }];

const mockHeaderService = {
  getHeaders: vi.fn().mockReturnValue(of(mockHeaders)),
};

const mockCategoryService = {
  postCategory: vi.fn().mockReturnValue(of({ id: 'cat-1' })),
  postCategoryAdministartors: vi.fn().mockReturnValue(of(undefined)),
  postCategoryAsync: vi.fn().mockResolvedValue({ id: 'cat-1' }),
  postCategoryAdministartorsAsync: vi.fn().mockResolvedValue(undefined),
};

const mockUserService = {
  getUsersAsync: vi.fn().mockResolvedValue([
    { userId: 'user1', firstname: 'John', lastname: 'Doe' },
    { userId: 'user2', firstname: 'Jane', lastname: 'Smith' },
  ]),
};

describe('CreateCategoryComponent', () => {
  let component: CreateCategoryComponent;
  let fixture: ComponentFixture<CreateCategoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateCategoryComponent],
      providers: [
        { provide: HeaderService, useValue: mockHeaderService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: UserService, useValue: mockUserService },
        provideTransloco({
          config: { defaultLang: 'en', availableLangs: ['en'] },
        }),
        {
          provide: TRANSLOCO_LOADER,
          useValue: { getTranslation: vi.fn().mockReturnValue(of({})) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateCategoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load headers via rxResource', () => {
    expect(component.headersResource.value()).toEqual(mockHeaders);
  });

  it('should initialize forms', () => {
    expect(component.categoryForm).toBeDefined();
    expect(component.addUserForm).toBeDefined();
  });

  describe('cancel', () => {
    it('should reset forms and state', () => {
      component.futureAdmins.set([{ userId: 'user1' }]);
      component.availableUsers.set([{ userId: 'user2' }]);

      component.cancel();

      expect(component.futureAdmins()).toEqual([]);
      expect(component.availableUsers()).toEqual([]);
      expect(component.categoryForm.controls['name'].value).toBe('');
      expect(component.categoryForm.controls['header'].value).toBe('');
    });
  });

  describe('searchUsers', () => {
    it('should populate users when name is not empty', async () => {
      component.addUserForm.controls['name'].setValue('john');

      await component.searchUsers();

      expect(mockUserService.getUsersAsync).toHaveBeenCalledWith({
        query: 'john',
        filter: false,
      });
      expect(component.availableUsers()).toHaveLength(2);
      expect(component.searchingUsers()).toBe(false);
    });

    it('should not search when name is empty', async () => {
      mockUserService.getUsersAsync.mockClear();
      component.addUserForm.controls['name'].setValue('');

      await component.searchUsers();

      expect(mockUserService.getUsersAsync).not.toHaveBeenCalled();
    });
  });

  describe('selectUsers', () => {
    it('should add selected users to futureAdmins', () => {
      component.availableUsers.set([
        { userId: 'user1', firstname: 'John', lastname: 'Doe' },
        { userId: 'user2', firstname: 'Jane', lastname: 'Smith' },
      ]);
      component.addUserForm.controls['possibleUsers'].setValue(['user1']);

      component.selectUsers();

      expect(component.futureAdmins()).toHaveLength(1);
      expect(component.futureAdmins()[0].userId).toBe('user1');
    });

    it('should not add duplicate users', () => {
      component.futureAdmins.set([{ userId: 'user1' }]);
      component.availableUsers.set([{ userId: 'user1' }]);
      component.addUserForm.controls['possibleUsers'].setValue(['user1']);

      component.selectUsers();

      expect(component.futureAdmins()).toHaveLength(1);
    });
  });

  describe('removeFromFutureAdmin', () => {
    it('should remove the specified user', () => {
      const user = { userId: 'user1' };
      component.futureAdmins.set([user, { userId: 'user2' }]);

      component.removeFromFutureAdmin(user);

      expect(component.futureAdmins()).toHaveLength(1);
      expect(component.futureAdmins()[0].userId).toBe('user2');
    });
  });

  describe('isWizardOk', () => {
    it('should return false when form is invalid', () => {
      component.futureAdmins.set([{ userId: 'user1' }]);
      expect(component.isWizardOk()).toBe(false);
    });

    it('should return false when no admins selected', () => {
      component.categoryForm.controls['name'].setValue('Test');
      component.categoryForm.controls['header'].setValue('h1');
      component.futureAdmins.set([]);

      expect(component.isWizardOk()).toBe(false);
    });

    it('should return true when form is valid and admins exist', () => {
      component.categoryForm.controls['name'].setValue('Test');
      component.categoryForm.controls['header'].setValue('h1');
      component.futureAdmins.set([{ userId: 'user1' }]);

      expect(component.isWizardOk()).toBe(true);
    });
  });

  describe('createCategory', () => {
    beforeEach(() => {
      mockCategoryService.postCategory.mockClear();
      mockCategoryService.postCategoryAdministartors.mockClear();
      mockCategoryService.postCategoryAsync.mockClear();
      mockCategoryService.postCategoryAdministartorsAsync.mockClear();
      mockCategoryService.postCategoryAsync.mockResolvedValue({ id: 'cat-1' });
      mockCategoryService.postCategoryAdministartorsAsync.mockResolvedValue(
        undefined
      );
    });

    it('should create category and assign admins', async () => {
      component.categoryForm.controls['name'].setValue('NewCat');
      component.categoryForm.controls['header'].setValue('h1');
      component.futureAdmins.set([{ userId: 'admin1' }, { userId: 'admin2' }]);

      await component.createCategory();

      expect(mockCategoryService.postCategoryAsync).toHaveBeenCalledWith({
        id: 'h1',
        category: {
          name: 'NewCat',
          title: '',
          header: 'h1',
        },
      });
      expect(
        mockCategoryService.postCategoryAdministartorsAsync
      ).toHaveBeenCalledWith({
        id: 'cat-1',
        requestBody: ['admin1', 'admin2'],
      });
      expect(component.processing()).toBe(false);
    });

    it('should handle error gracefully', async () => {
      mockCategoryService.postCategoryAsync.mockRejectedValueOnce(
        new Error('fail')
      );
      const consoleSpy = vi
        .spyOn(console, 'error')
        .mockImplementation(() => {});

      await component.createCategory();

      expect(consoleSpy).toHaveBeenCalledWith(
        'Error during the creation of the category'
      );
      expect(component.processing()).toBe(false);
      consoleSpy.mockRestore();
    });
  });

  describe('hasSelectedUser', () => {
    it('should return true when a user is selected', () => {
      component.addUserForm.controls['possibleUsers'].setValue(['user1']);
      expect(component.hasSelectedUser()).toBe(true);
    });

    it('should return false when no user is selected', () => {
      component.addUserForm.controls['possibleUsers'].setValue('');
      expect(component.hasSelectedUser()).toBe(false);
    });
  });

  describe('resetForm', () => {
    it('should clear available users and form fields', () => {
      component.availableUsers.set([{ userId: 'user1' }]);
      component.addUserForm.controls['name'].setValue('test');
      component.addUserForm.controls['possibleUsers'].setValue('val');

      component.resetForm();

      expect(component.availableUsers()).toEqual([]);
      expect(component.addUserForm.controls['name'].value).toBe('');
      expect(component.addUserForm.controls['possibleUsers'].value).toBe('');
    });
  });
});
